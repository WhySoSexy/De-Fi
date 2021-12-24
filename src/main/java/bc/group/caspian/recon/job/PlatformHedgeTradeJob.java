package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.platform.hedgeTrades.PlatformHedgeFloatTransaction;
import bc.group.caspian.recon.api.platform.hedgeTrades.PlatformHedgeTrade;
import bc.group.caspian.recon.api.platform.hedgeTrades.PlatformHedgeTradeResponse;
import bc.group.caspian.recon.domain.mysql.PlatformHedgeTradeEntity;
import bc.group.caspian.recon.domain.mysql.SchedulerStatusEntity;
import bc.group.caspian.recon.domain.reconInflux.PlatformHedgeTradeMeasurement;
import bc.group.caspian.recon.repository.PlatformHedgeTradeRepository;
import bc.group.caspian.recon.repository.ScheduledStatusRepository;
import bc.group.caspian.recon.service.DataFeedService;
import bc.group.caspian.recon.service.PlatformHedgeTradeService;
import bc.group.caspian.recon.service.api.platform.PlatformEndpoint;
import bc.group.caspian.recon.service.api.platform.Source;
import bc.group.caspian.recon.service.config.PlatformHedgeTradeProperties;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlatformHedgeTradeJob {

    private final DataFeedService dataFeedService;
    private final PlatformHedgeTradeService platformHedgeTradeService;
    private final ScheduledStatusRepository scheduledStatusRepository;
    private final PlatformHedgeTradeRepository repository;
    private final PlatformHedgeTradeProperties properties;
    private final static Logger logger = LoggerFactory.getLogger(PlatformHedgeTradeJob.class);

    public PlatformHedgeTradeJob(DataFeedService dataFeedService, PlatformHedgeTradeService platformHedgeTradeService, ScheduledStatusRepository scheduledStatusRepository, PlatformHedgeTradeRepository repository, PlatformHedgeTradeProperties properties) {
        this.dataFeedService = dataFeedService;
        this.properties = properties;
        this.repository = repository;
        this.platformHedgeTradeService = platformHedgeTradeService;
        this.scheduledStatusRepository = scheduledStatusRepository;
    }

    public void runPlatformHedgeTradeJob() {
        try {
            PlatformHedgeTradeResponse response = platformHedgeTradeService.getPlatformHedgeTrades();

            if (response != null && response.getResult() != null) {
                List<PlatformHedgeTrade> trades = response.getResult().getTrades();
                List<String> ids = trades.stream().map(PlatformHedgeTrade::getId).collect(Collectors.toList());
                logger.info("Got {} new hedge trades from Platform api, tradeId: {}", trades.size(), ids);

                List<PlatformHedgeTrade> validTrades = getValidTrades(trades);
                List<String> validIds = validTrades.stream().map(PlatformHedgeTrade::getId).collect(Collectors.toList());
                logger.info("Got {} valid hedge trades from Platform api, tradeId: {}", validTrades.size(), validIds);

                validTrades.forEach(this::process);
                updateStatus(response);
            } else {
                logger.info("Failed to process hedge trades, value is null");
            }
        } catch (Exception e) {
            logger.error("Failed to execute hedge job", e);
        }
    }

    public void runPlatformHedgeTradeRetryJob() {
        try {
            List<Long> invalidTrades = repository.findTradeIdByCompletedFalseOrderByTradeIdDesc();
            logger.info("Found {} uncompleted platform hedge trades, tradeId: {}", invalidTrades.size(), invalidTrades);

            if (invalidTrades.isEmpty()) return;

            List<List<Long>> batchIdLists = Lists.partition(invalidTrades, Integer.parseInt(properties.getBatchSize()));
            batchIdLists.forEach(e -> {
                PlatformHedgeTradeResponse response = platformHedgeTradeService.getPlatformHedgeTradesRetry(e);
                if (response != null && response.getResult() != null) {
                    List<PlatformHedgeTrade> trades = response.getResult().getTrades();
                    List<String> ids = trades.stream().map(PlatformHedgeTrade::getId).collect(Collectors.toList());
                    logger.info("Got {} new hedge trades from Platform api, tradeId: {}", trades.size(), ids);

                    List<PlatformHedgeTrade> validTrades = getValidTrades(response.getResult().getTrades());
                    List<String> validIds = validTrades.stream().map(PlatformHedgeTrade::getId).collect(Collectors.toList());
                    logger.info("Got {} valid hedge trades from Platform api, tradeId: {}", validTrades.size(), validIds);

                    validTrades.forEach(this::processRetry);
                } else {
                    logger.info("Failed to process hedge trades retry, value is null");
                }
            });
        } catch (
                Exception e) {
            logger.error("Failed to execute hedge retry job", e);
        }
    }

    protected void process(PlatformHedgeTrade trade) {
        PlatformHedgeTradeEntity existingTrade = repository.findPlatformHedgeTradeEntityByTradeId(Long.parseLong(trade.getId()));
        if (existingTrade == null) {
            existingTrade = PlatformHedgeTradeEntity.builder().tradeId(Long.parseLong(trade.getId())).completed(false).build();
            try {
                saveToInflux(trade);
                existingTrade.setCompleted(true);
                repository.save(existingTrade);
            } catch (Exception e) {
                logger.info("Failed to process platform hedge trade, tradeId: {}", trade.getId());
                repository.save(existingTrade);
                throw e;
            }
        }
    }

    protected void processRetry(PlatformHedgeTrade trade) {
        try {
            saveToInflux(trade);
            repository.updateCompletedInPlatformHedgeTradeId(Long.parseLong(trade.getId()), true);
        } catch (Exception e) {
            logger.info("Failed to process platform hedge trade retry, tradeId: {}", trade.getId());
            throw e;
        }
    }

    protected List<PlatformHedgeTrade> getValidTrades(List<PlatformHedgeTrade> trades) {
        List<PlatformHedgeTrade> validTrades = new ArrayList<>();
        trades.forEach(trade -> {
            if (trade.isValidHedgeTrade()) {
                validTrades.add(trade);
            } else {
                saveInvalidTrade(trade);
            }
        });
        return validTrades;
    }

    protected void saveToInflux(PlatformHedgeTrade trade) {
        for (PlatformHedgeFloatTransaction transaction : trade.getFloatTransactions()) {
            PlatformHedgeTradeMeasurement measurement = PlatformHedgeTradeMeasurement.builder()
                    .amount(String.valueOf(transaction.getAmount()))
                    .ccy(transaction.getCcy())
                    .side(transaction.getTxnType())
                    .userUuid(transaction.getUserUuid())
                    .siteGroup(transaction.getSiteGroup())
                    .processedDate(transaction.getProcessedDateTime())
                    .transactionUuid(trade.getUuid())
                    .tradeId(trade.getId())
                    .source(Source.PLATFORM.name())
                    .buyTradedCurrency(String.valueOf(trade.getBuyTradedCurrency()))
                    .build();
            dataFeedService.publishToInflux(measurement);
        }
    }

    protected void saveInvalidTrade(PlatformHedgeTrade trade) {
        PlatformHedgeTradeEntity entity = repository.findPlatformHedgeTradeEntityByTradeId(Long.parseLong(trade.getId()));
        if (entity == null) {
            entity = PlatformHedgeTradeEntity.builder().tradeId(Long.parseLong(trade.getId())).completed(false).build();
            repository.save(entity);
            logger.info("Saved as invalid trade with tradeId: {}", trade.getId());
        }
    }

    protected void updateStatus(PlatformHedgeTradeResponse response) {
        SchedulerStatusEntity status = scheduledStatusRepository.findSchedulerStatusEntityByName(PlatformEndpoint.HEDGE.name());
        status = status != null ? processStatus(status, response) : getStatus(response);
        scheduledStatusRepository.save(status);
    }

    protected SchedulerStatusEntity processStatus(SchedulerStatusEntity status, PlatformHedgeTradeResponse response) {
        return isCompleted(response, status) ? setOffsetZeroAndUpdateTs(status) : updateOffset(status, response);
    }

    protected SchedulerStatusEntity getStatus(PlatformHedgeTradeResponse response) {
        if (response.getResult().getTrades().size() == response.getResult().getTotalCount()) {
            return SchedulerStatusEntity.builder()
                    .name(PlatformEndpoint.HEDGE.name())
                    .fromTs(getDateMinusSeconds(getDateWithBatch(properties.getStartDate()).getTime()))
                    .toTs(getDateWithBatch(getDateWithBatch(properties.getStartDate()).getTime()))
                    .offset(0L)
                    .build();
        }

        return SchedulerStatusEntity.builder()
                .name(PlatformEndpoint.HEDGE.name())
                .fromTs(getDateMinusSeconds(properties.getStartDate()))
                .toTs(getDateWithBatch(properties.getStartDate()))
                .offset(getOffset(response))
                .build();
    }

    protected SchedulerStatusEntity setOffsetZeroAndUpdateTs(SchedulerStatusEntity status) {
        status.setOffset(0L);
        status.setFromTs(getDateMinusSeconds(status.getToTs().getTime()));
        status.setToTs(getDateWithBatch(status.getToTs().getTime()));
        return status;
    }

    protected SchedulerStatusEntity updateOffset(SchedulerStatusEntity status, PlatformHedgeTradeResponse response) {
        status.setOffset(getOffsetAndListSizeSum(status, response));
        return status;
    }

    protected boolean isCompleted(PlatformHedgeTradeResponse response, SchedulerStatusEntity status) {
        return getOffsetAndListSizeSum(status, response).equals(response.getResult().getTotalCount());
    }

    protected Timestamp getDateWithBatch(Long timestamp) {
        LocalDateTime date = new Timestamp(timestamp).toLocalDateTime().plusDays(5L);
        if (date.isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
            return Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return Timestamp.valueOf(date);
    }

    protected Timestamp getDateMinusSeconds(Long timestamp) {
        LocalDateTime date = new Timestamp(timestamp).toLocalDateTime().minusSeconds(10L);
        return Timestamp.valueOf(date);
    }

    protected Long getOffset(PlatformHedgeTradeResponse response) {
        return (long) response.getResult().getTrades().size();
    }

    protected Long getOffsetAndListSizeSum(SchedulerStatusEntity status, PlatformHedgeTradeResponse response) {
        return Long.sum(status.getOffset(), response.getResult().getTrades().size());
    }
}
