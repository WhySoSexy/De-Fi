package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.cep.CepHedgeTrade;
import bc.group.caspian.recon.api.platform.rfqTrades.PlatformRfqTrade;
import bc.group.caspian.recon.api.platform.rfqTrades.PlatformRfqTradeResult;
import bc.group.caspian.recon.domain.mysql.PlatformRfqTradeEntity;
import bc.group.caspian.recon.domain.reconInflux.ClientTradeMeasurement;
import bc.group.caspian.recon.domain.reconInflux.PlatformRfqTradeMeasurement;
import bc.group.caspian.recon.repository.PlatformRfqTradeRepository;
import bc.group.caspian.recon.service.DataFeedService;
import bc.group.caspian.recon.service.PlatformRfqTradeService;
import bc.group.caspian.recon.service.config.PlatformRfqTradeProperties;
import com.google.common.collect.Lists;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlatformRfqTradeJob {

    private final DataFeedService dataFeedService;
    private final PlatformRfqTradeService platformRfqTradeService;
    private final PlatformRfqTradeRepository platformRfqTradeRepository;

    protected final PlatformRfqTradeProperties properties;

    Counter tradesCounter;
    Counter pushedReconCounter;

    private final static Logger logger = LoggerFactory.getLogger(PlatformRfqTradeJob.class);

    public PlatformRfqTradeJob(
            DataFeedService dataFeedService, PlatformRfqTradeService platformRfqTradeService,
            PlatformRfqTradeRepository platformRfqTradeRepository, PlatformRfqTradeProperties balanceProperties,
            MeterRegistry meterRegistry
    ) {
        this.dataFeedService = dataFeedService;
        this.platformRfqTradeService = platformRfqTradeService;
        this.platformRfqTradeRepository = platformRfqTradeRepository;
        this.properties = balanceProperties;

        String GAUGE_METRICS = "gauge.metrics";

        tradesCounter = Counter.builder(GAUGE_METRICS)
                .tags("type","tradesSize")
                .description("The number of not inserted rfq trades grabbed from platform api")
                .register(meterRegistry);

        pushedReconCounter = Counter.builder(GAUGE_METRICS)
                .tags("type","pushReconSize")
                .description("The number of rfq trades push into influxdb")
                .register(meterRegistry);
    }

    public void runTradesJob() {
        PlatformRfqTradeEntity lastTrade = platformRfqTradeRepository.findFirstByOrderByTradeIdDesc();
        String lastTimestamp = lastTrade == null ? properties.getStartDate() : lastTrade
                .getDateCreated()
                .toInstant()
                .minusSeconds(1).toString();
        String currentTimestamp = LocalDateTime.now()
                .minusMinutes(1)
                .truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
                .toInstant(ZoneOffset.UTC).toString();
        long offset = 0L;
        long processedCount = 0L;
        long totalCount = 0L;

        do {
            try {
                Optional<PlatformRfqTradeResult> optionalPlatformRfqTradeResult = platformRfqTradeService
                        .getRfqTradesResult(
                                lastTimestamp, currentTimestamp, properties.getSiteGroups(), offset, properties.getBatchSize()
                        );
                if (!optionalPlatformRfqTradeResult.isPresent()) {
                    logger.info("Failed to get rfq trades from Platform api, result is null");
                    return;
                }
                PlatformRfqTradeResult result = optionalPlatformRfqTradeResult.orElseThrow(
                        IllegalArgumentException::new);

                List<PlatformRfqTrade> trades = result.getTrades();
                int resultCount = trades.size();
                List<String> tradeIds = trades.stream().map(PlatformRfqTrade::getTradeId).collect(Collectors.toList());
                logger.info("Fetched {} new rfq trades from Platform api tradeIds: {}", resultCount, tradeIds);

                int noOfDataPushedToInflux = process(trades);

                offset += resultCount;
                processedCount += resultCount;
                totalCount = result.getTotalCount();

                logger.info("Completed pushing {} rfq and client trades from Platform api of {} fetched trades to influx from platform",
                        noOfDataPushedToInflux, resultCount);

            } catch (Exception e) {
                logger.error("Failed to execute rfq trades job", e);
            }
        } while (processedCount < totalCount);
    }

    public void runTradesRetryJob() {
        try {
            List<String> notInsertedTrades = platformRfqTradeRepository.findTradeIdByCompletedFalseOrderByTradeIdAsc();
            if (notInsertedTrades.isEmpty()) return;

            List<List<String>> batchIdLists = Lists.partition(notInsertedTrades, properties.getBatchSize().intValue());
            batchIdLists.forEach(batch -> {
                Optional<PlatformRfqTradeResult> optionalPlatformRfqTradeResult = platformRfqTradeService
                        .getRfqTradesResult(
                                batch, 0L, properties.getBatchSize()
                        );
                if (!optionalPlatformRfqTradeResult.isPresent()) {
                    logger.info("Failed to get rfq trades from Platform api, result is null");
                    return;
                }
                PlatformRfqTradeResult result = optionalPlatformRfqTradeResult.orElseThrow(
                        IllegalArgumentException::new);
                List<PlatformRfqTrade> trades = result.getTrades();
                int resultCount = trades.size();
                List<String> tradeIds = trades.stream().map(PlatformRfqTrade::getTradeId).collect(Collectors.toList());
                logger.info("Re-Fetched {} rfq trades from Platform api tradeIds: {}", resultCount, tradeIds);

                int noOfDataPushedToInflux = process(trades);

                pushedReconCounter.increment(noOfDataPushedToInflux);

                logger.info("Completed re-pushing {} rfq and client trades from Platform api of {} fetched trades to influx from platform",
                        noOfDataPushedToInflux, resultCount);
            });
        } catch (
                Exception e) {
            logger.error("Failed to execute platform rfq trades retry job", e);
        }
    }

    protected int process(List<PlatformRfqTrade> trades) {
        List<PlatformRfqTradeEntity> entities =  trades
                .stream()
                .map(this::process)
                .collect(Collectors.toList());
        platformRfqTradeRepository.saveAll(entities);

        int noOfDataPushedToInflux = (int) entities
                .stream()
                .filter(PlatformRfqTradeEntity::getCompleted)
                .count();

        return noOfDataPushedToInflux;
    }

    protected PlatformRfqTradeEntity process(PlatformRfqTrade trade) {
        PlatformRfqTradeEntity entity = getPlatformRfqTradeEntity(trade);
        if (isNotNeedToBeProcessed(trade)) {
            logger.info("Rfq platform Trade {} was already processed", trade.getTradeId());
            return entity;
        }

        logger.info("Start processing trade: {}", trade.getTradeId());
        boolean isCompletedRfqTrade = trade.containsSiteGroupForHedgeTradeMeasurement() ? publishRfqTradeToInflux(trade) : true;
        boolean isCompletedClient = trade.containsSiteGroupForClientTradeMeasurement() ? publishClientTradeToInflux(trade) : true;

        entity.setCompleted(isCompletedRfqTrade && isCompletedClient);
        logger.info("End processing trade: {}", trade.getTradeId());
        return entity;
    }

    protected boolean publishRfqTradeToInflux(PlatformRfqTrade trade) {
        PlatformRfqTradeMeasurement tm = dataFeedService.getTransactionDto(trade, PlatformRfqTradeMeasurement.class);

        try {
            dataFeedService.publishToInflux(tm);
        } catch (Exception e) {
            logger.error("Failed to push rfq platform trade {} into influx", tm.getTradeId(), e);
            return false;
        }

        return true;
    }

    protected boolean publishClientTradeToInflux(PlatformRfqTrade trade) {
        ClientTradeMeasurement tm = dataFeedService.getTransactionDto(trade, ClientTradeMeasurement.class);

        try {
            dataFeedService.publishToInflux(tm);
        } catch (Exception e) {
            logger.error("Failed to push client trade {} into influx", tm.getTradeId(), e);
            return false;
        }

        return true;
    }

    private PlatformRfqTradeEntity getPlatformRfqTradeEntity(PlatformRfqTrade trade) {
        PlatformRfqTradeEntity entity = platformRfqTradeRepository.findFirstByTradeId(trade.getTradeId());
        if (entity == null) {
            return dataFeedService.getTransactionDto(trade, PlatformRfqTradeEntity.class);
        }

        return entity;
    }

    protected boolean isNotNeedToBeProcessed(PlatformRfqTrade trade) {
        PlatformRfqTradeEntity entity = platformRfqTradeRepository.findFirstByTradeId(trade.getTradeId());
        String lastUpdatedAt = ZonedDateTime.parse(trade.getLastUpdated(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]"))
                .withZoneSameInstant(ZoneOffset.UTC).toInstant().toString();
        return entity != null && entity.getId() != null && entity.getLastUpdated() != null
                && (entity.getLastUpdated().toInstant().toString().equals(lastUpdatedAt))
                && entity.getCompleted() == true;
    }
}
