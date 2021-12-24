package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.cep.CepHedgeTrade;
import bc.group.caspian.recon.api.cep.CepHedgeTradeResponse;
import bc.group.caspian.recon.domain.reconInflux.CepHedgeTradeMeasurement;
import bc.group.caspian.recon.repository.CepHedgeTradeRepository;
import bc.group.caspian.recon.service.CepHedgeTradeService;
import bc.group.caspian.recon.service.DataFeedService;
import bc.group.caspian.recon.service.config.CepHedgeTradeRetryProperties;
import com.google.common.collect.Lists;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class CepHedgeTradeRetryJob {

    private final static Logger logger = LoggerFactory.getLogger(CepHedgeTradeRetryJob.class);
    private final Counter uncompletedTradesCounter;
    private final Counter pushedToInfluxTradesCounter;
    private final CepHedgeTradeService cepHedgeTradeService;
    private final CepHedgeTradeRepository repository;
    private final DataFeedService dataFeedService;
    private final CepHedgeTradeRetryProperties properties;

    public CepHedgeTradeRetryJob(MeterRegistry meterRegistry, CepHedgeTradeRepository repository, CepHedgeTradeService cepHedgeTradeService, DataFeedService dataFeedService, CepHedgeTradeRetryProperties properties) {
        this.dataFeedService = dataFeedService;
        this.repository = repository;
        this.properties = properties;
        this.cepHedgeTradeService = cepHedgeTradeService;
        String GAUGE_METRICS = "gauge.metrics";

        uncompletedTradesCounter = Counter.builder(GAUGE_METRICS)
                .tags("type", "uncompletedTradesSize")
                .description("The number of completed cep hedge trades re-fetched from cep api")
                .register(meterRegistry);

        pushedToInfluxTradesCounter = Counter.builder(GAUGE_METRICS)
                .tags("type", "pushedTradesSize")
                .description("The number of re-fetched cep hedge trades successfully pushed into influx")
                .register(meterRegistry);
    }

    public void runHedgeRetryJob() {
        List<Long> uncompletedIds = repository.findIdByCompletedFalseOrderByIdDesc();
        logger.info("Found {} uncompleted hedge trades from CEP api", uncompletedIds.size());

        if (!uncompletedIds.isEmpty()) {
            List<List<Long>> idsBatches = Lists.partition(uncompletedIds, Integer.parseInt(properties.getBatchSize()));

            idsBatches.forEach(this::process);
        }
    }

    private void process(List<Long> ids) {
        logger.info("Query cep api with {} id list", ids.toString());
        CepHedgeTradeResponse response = cepHedgeTradeService.getResponseRetry(ids);

        if (response != null && response.getRfqTradeList() != null) {
            updateExistingIds(response, ids);

            List<CepHedgeTradeMeasurement> measurements = cepHedgeTradeService.process(response);
            List<String> cepIds = measurements.stream().map(CepHedgeTradeMeasurement::getCepId).collect(Collectors.toList());
            List<String> fillIds = measurements.stream().map(CepHedgeTradeMeasurement::getHedgeId).collect(Collectors.toList());
            logger.info("Processed {} cep trades with retry cepId: {}, fillId: {}", measurements.size(), cepIds, fillIds);

            uncompletedTradesCounter.increment(measurements.size());

            AtomicInteger noOfDataPushedToInflux = new AtomicInteger();

            measurements.forEach(element -> {
                try {
                    dataFeedService.publishToInflux(element);
                    repository.updateCompletedInCepId(Long.parseLong(element.getCepId()), true);
                    noOfDataPushedToInflux.getAndIncrement();
                } catch (Exception e) {
                    logger.info("Failed to re-push cep hedge (retry) into influx cepId: {}, hedgeId: {}", element.getCepId(), element.getHedgeId(), e);
                }
            });
            logger.info("Completed re-pushing {} cep hedge trades to influx fillId: {}", noOfDataPushedToInflux.intValue(), fillIds);
            pushedToInfluxTradesCounter.increment(noOfDataPushedToInflux.intValue());
        }

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            logger.info("Failed to pause cep hedge retry job", e);
        }
    }

    private Boolean isExisting(CepHedgeTradeResponse response, Long id) {
        boolean isContains = response.getRfqTradeList()
                .stream()
                .anyMatch(trade -> id.equals(Long.parseLong(trade.getId())));
        if (isContains) {
            for (CepHedgeTrade trade : response.getRfqTradeList()) {
                if (trade.getId().equals(String.valueOf(id))) {
                    logger.info("Checking if existing cepId {}, status {} ", trade.getId(), trade.getStatus());
                    return !trade.getStatus().equalsIgnoreCase(properties.getInvalid());
                }
            }
        }
        return isContains;
    }

    private void updateExistingIds(CepHedgeTradeResponse response, List<Long> ids) {
        for (Long id : ids) {
            if (id < Long.parseLong(response.getLatestId())) {
                boolean existing = isExisting(response, id);
                repository.updateExistingInCepId(id, existing);
                logger.info("Updated existing {} in cep trade where cepId: {}", existing, id);
            }
        }
    }
}
