package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.caspian.PositionPortfolio;
import bc.group.caspian.recon.config.caspian.PositionsProperties;
import bc.group.caspian.recon.domain.reconInflux.PositionMeasurement;
import bc.group.caspian.recon.repository.PlatformRfqTradeRepository;
import bc.group.caspian.recon.service.DataFeedService;
import bc.group.caspian.recon.service.PlatformRfqTradeService;
import bc.group.caspian.recon.service.PositionService;
import bc.group.caspian.recon.service.config.PlatformRfqTradeProperties;
import group.bc.caspian.connector.model.Position;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Profile("!test")
@Service
public class PositionsJob {

    private final static Logger logger = LoggerFactory.getLogger(PositionsJob.class);

    private final PositionService positionService;
    private final DataFeedService dataFeedService;
    private final PositionsProperties positionsProperties;

    Counter positionsCounter;
    Counter pushedToInfluxPositionsCounter;

    public PositionsJob(
            DataFeedService dataFeedService, PositionService positionService,
            PositionsProperties positionsProperties,
            MeterRegistry meterRegistry
    ) {
        this.dataFeedService = dataFeedService;
        this.positionService = positionService;
        this.positionsProperties = positionsProperties;

        String GAUGE_METRICS = "gauge.metrics";

        positionsCounter = Counter.builder(GAUGE_METRICS)
                .tags("type","positionsSize")
                .description("The number of positions grab from caspian api")
                .register(meterRegistry);
        pushedToInfluxPositionsCounter = Counter.builder(GAUGE_METRICS)
                .tags("type", "pushedPositionsSize")
                .description("The number of positions successfully pushed into influx")
                .register(meterRegistry);
    }

    public void runPositionsJob() throws RestClientException {
        try {
            Optional<List<Position>> positionList = Optional.ofNullable(
                    positionService.getPositions("REALTIME", "acquirer")
            );

            positionList.ifPresent(positions -> {
                List<Position> supportedPositions = positions.stream()
                        .filter(this::isSupportedPosition)
                        .collect(Collectors.toList());
                logger.info("Got {} supported positions from Caspian PMS", supportedPositions.size());
                positionsCounter.increment(positions.size());

                List<PositionMeasurement> measurements = transformToMeasurements(supportedPositions)
                        .stream()
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                AtomicInteger noOfDataPushedToInflux = new AtomicInteger();
                measurements.forEach(m -> {
                    try {
                        dataFeedService.publishToInflux(m);
                        noOfDataPushedToInflux.getAndIncrement();
                    } catch (Exception e) {
                        logger.info("Failed to push positions into influx", e);
                    }
                });

                logger.info("Completed pushing {} positions to influx ", noOfDataPushedToInflux.intValue());
                pushedToInfluxPositionsCounter.increment(noOfDataPushedToInflux.intValue());
            });
        } catch (RestClientException err) {
            logger.error("Failed to get positions from Caspian PMS ", err);
            throw err;
        } catch (Exception err) {
            logger.error("Error occurs ", err);
            throw err;
        }
    }

    private boolean isSupportedPosition(Position position) {
        return positionsProperties.getSupported().stream().filter(property ->
                (StringUtils.isEmpty(property.getFund()) || property.getFund().equals(position.getFund()))
                        && (StringUtils.isEmpty(property.getPortfolio()) || property.getPortfolio().equals(position.getPortfolio()))
                        && (StringUtils.isEmpty(property.getStrategy()) || property.getStrategy().equals(position.getStrategy()))
                        && (CollectionUtils.isEmpty(property.getCustodian()) || property.getCustodian().contains(position.getCustodian()))
        ).count() > 0;
    }

    protected List<Optional<PositionMeasurement>> transformToMeasurements(List<Position> positions) {
        Map<String, Map<String, Map<String, List<Position>>>> positionsMapByAccount = positions.stream()
                .collect(
                        Collectors.groupingBy(
                                Position::getCustodian,
                                Collectors.groupingBy(
                                        Position::getInstrument, Collectors.groupingBy(
                                                Position::getFund
                                        )
                                )
                        )
                );

        List<List<Position>> positionsGroupedByCustodianInstrumentsFund = positionsMapByAccount.values().stream()
                .map(e -> new ArrayList<>(e.values()))
                .flatMap(List::stream)
                .collect(Collectors.toList())
                .stream()
                .map(e -> new ArrayList<>(e.values()))
                .flatMap(List::stream)
                .collect(Collectors.toList());

       List<List<Position>> sumUpPositions = positionsGroupedByCustodianInstrumentsFund.stream()
               .map(this::groupedByPortfolio)
               .collect(Collectors.toList())
               .stream()
               .map(e -> new ArrayList<>(e.values()))
               .flatMap(List::stream)
               .collect(Collectors.toList());

        return  sumUpPositions.stream()
                .map(this::transformToMeasurement)
                .collect(Collectors.toList());
    }

    protected Map<String, List<Position>> groupedByPortfolio(List<Position> positions) {
        HashMap<String, List<Position>> groupedByPortfolio = new HashMap<>();
        positions.forEach(p -> {
            String portfolio = getAggregatedPortfolio(p);
            List<Position> portfolioPositions = groupedByPortfolio.getOrDefault(portfolio, new ArrayList<>());
            portfolioPositions.add(p);
            groupedByPortfolio.remove(portfolio);
            groupedByPortfolio.put(portfolio, portfolioPositions);
        });
        return groupedByPortfolio;
    }

    protected Optional<PositionMeasurement> transformToMeasurement(List<Position> feeds) {
        if (feeds.isEmpty()) return null;

        Position position = feeds.get(0);
        PositionMeasurement measurement = dataFeedService.getTransactionDto(position, PositionMeasurement.class);
        String portfolio = getAggregatedPortfolio(position);
        measurement.setPortfolio(portfolio);

        BigDecimal total = getSumOfBalances(feeds);
        measurement.setPosition(total.toString());

        logger.trace("position: " + feeds.toString());
        logger.trace("position measurement: " + measurement.toString());
        return Optional.of(measurement);
    }

    protected BigDecimal getSumOfBalances(List<Position> positions) {
        return positions.stream()
                .map(Position::getPosition)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    protected String getAggregatedPortfolio(Position position) {
        if (PositionPortfolio.isOTC(position.getPortfolio()) || PositionPortfolio.isExchange(position.getPortfolio()) ) {
            return PositionPortfolio.getOTCExchangePortfolio();
        }
        return position.getPortfolio();
    }
}
