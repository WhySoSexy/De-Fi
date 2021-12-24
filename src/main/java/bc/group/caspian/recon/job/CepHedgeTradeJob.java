package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.cep.CepHedgeTrade;
import bc.group.caspian.recon.api.cep.CepHedgeTradeResponse;
import bc.group.caspian.recon.domain.mysql.CepHedgeTradeEntity;
import bc.group.caspian.recon.domain.reconInflux.CepHedgeTradeMeasurement;
import bc.group.caspian.recon.repository.CepHedgeTradeRepository;
import bc.group.caspian.recon.service.DataFeedService;
import bc.group.caspian.recon.service.CepHedgeTradeService;
import bc.group.caspian.recon.service.config.CepHedgeTradeProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class CepHedgeTradeJob {

    private final static Logger logger = LoggerFactory.getLogger(CepHedgeTradeJob.class);
    private final Counter completedTradesCounter;
    private final Counter pushedToInfluxTradesCounter;
    private final CepHedgeTradeService cepHedgeTradeService;
    private final CepHedgeTradeRepository repository;
    private final DataFeedService dataFeedService;
    private final CepHedgeTradeProperties properties;

    public CepHedgeTradeJob(MeterRegistry meterRegistry, CepHedgeTradeService cepHedgeTradeService, DataFeedService dataFeedService, CepHedgeTradeProperties properties, CepHedgeTradeRepository repository) {
        this.dataFeedService = dataFeedService;
        this.properties = properties;
        this.repository = repository;
        this.cepHedgeTradeService = cepHedgeTradeService;
        String GAUGE_METRICS = "gauge.metrics";

        completedTradesCounter = Counter.builder(GAUGE_METRICS)
                .tags("type", "completedTradesSize")
                .description("The number of completed cep hedge trades fetched from cep api")
                .register(meterRegistry);

        pushedToInfluxTradesCounter = Counter.builder(GAUGE_METRICS)
                .tags("type", "pushedTradesSize")
                .description("The number of completed cep hedge trades successfully pushed into influx")
                .register(meterRegistry);
    }

    public void runHedgeJob() {
        Long id = getId();

        while (true) {
            CepHedgeTradeResponse response = cepHedgeTradeService.getResponse(id);
            if (response != null && response.getRfqTradeList() != null) {
                process(response);
                if (id > Long.parseLong(response.getLatestId()) || !response.getRfqTradeList().isEmpty()) {
                    return;
                }
                id += properties.getBatchSize();
            }
        }
    }

    protected void process(CepHedgeTradeResponse response) {
        List<String> cepIds = response.getRfqTradeList().stream().map(CepHedgeTrade::getId).collect(Collectors.toList());
        List<String> fillIds = response.getRfqTradeFillList().stream().map(CepHedgeTrade::getFillId).collect(Collectors.toList());
        logger.info("Fetched {} new hedge trades from CEP api cepId: {}, fillId: {}", response.getRfqTradeList().size(), cepIds, fillIds);

        List<CepHedgeTradeMeasurement> measurements = cepHedgeTradeService.process(response);
        updateIds(response);

        List<String> measurementCepIds = measurements.stream().map(CepHedgeTradeMeasurement::getCepId).collect(Collectors.toList());
        List<String> hedgeIds = measurements.stream().map(CepHedgeTradeMeasurement::getHedgeId).collect(Collectors.toList());
        logger.info("Processed {} new hedge trades from CEP api cepId: {}, hedgeId: {}", measurements.size(), measurementCepIds, hedgeIds);
        completedTradesCounter.increment(measurements.size());

        AtomicInteger noOfDataPushedToInflux = new AtomicInteger();

        measurements.forEach(element -> {
            try {
                dataFeedService.publishToInflux(element);
                noOfDataPushedToInflux.getAndIncrement();
            } catch (Exception e) {
                logger.info("Failed to push cep hedge into influx cepId: {}, hedgeId: {}", element.getCepId(), element.getHedgeId(), e);
                updateUncompleted(element.getCepId());
            }
        });
        logger.info("Completed pushing {} cep hedge trades to influx ", noOfDataPushedToInflux.intValue());
        pushedToInfluxTradesCounter.increment(noOfDataPushedToInflux.intValue());
    }

    protected Long getId() {
        Optional<CepHedgeTradeEntity> previous = repository.findTopByOrderByIdDesc();

        if (previous.isPresent()) {
            Long id = previous.get().getId();
            return ++id;
        }
        return properties.getStartId();
    }

    protected int updateIds(CepHedgeTradeResponse response) {
        try {
            List<CepHedgeTradeEntity> entities = new ArrayList<>();

            response.getRfqTradeList().forEach(client -> {
                CepHedgeTradeEntity entity = CepHedgeTradeEntity.builder()
                        .id(Long.parseLong(client.getId()))
                        .completed(isCompleted(client))
                        .existing(true)
                        .build();
                entities.add(entity);
            });

            List<Long> notCompleted = entities.stream()
                    .filter(e -> e.getCompleted().equals(false))
                    .map(CepHedgeTradeEntity::getId)
                    .collect(Collectors.toList());
            logger.info("Saved {} trades, {} saved with completed false due to non hedged status cepId: {}",
                    entities.size(), notCompleted.size(), notCompleted);

            repository.saveAll(entities);

            return entities.size();
        } catch (Exception e) {
            logger.info("Failed to update last hedge id", e);
            throw e;
        }
    }

    protected Boolean isCompleted(CepHedgeTrade trade) {
        return properties.getValid().contains(trade.getStatus());
    }

    protected void updateUncompleted(String cepId) {
        repository.updateCompletedInCepId(Long.parseLong(cepId), false);
    }
}

