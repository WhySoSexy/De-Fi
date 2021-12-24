package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.caspian.PositionPortfolio;
import bc.group.caspian.recon.config.caspian.PositionsProperties;
import bc.group.caspian.recon.domain.reconInflux.PlatformRfqTradeMeasurement;
import bc.group.caspian.recon.domain.reconInflux.PositionMeasurement;
import bc.group.caspian.recon.service.DataFeedService;
import group.bc.caspian.connector.model.Position;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class PositionsJobTest {

    @InjectMocks
    private PositionsJob scheduler;

    @Mock
    private DataFeedService dataFeedService;

    @Mock
    private PositionsProperties properties;

    @Mock
    MeterRegistry meterRegistry;

    @Test
    public void transformToMeasurementsTest() {
        Position position = new Position();
        position.setPortfolio("OTCFLOW");
        position.setStrategy("OTCFLOW");
        position.setInstrument("BTC");
        position.setPosition(new BigDecimal("1.0"));
        position.setFund("Fund OSLSG");
        position.setCustodian("custodian1");

        Position position1 = new Position();
        position1.setPortfolio("OTCFLOW");
        position1.setStrategy("OTCFLOW");
        position1.setInstrument("BCH");
        position1.setPosition(new BigDecimal("10.0"));
        position1.setFund("Fund OSLSG");
        position1.setCustodian("custodian1");

        Position position2 = new Position();
        position2.setPortfolio("OTCFLOW");
        position2.setStrategy("OTCFLOW");
        position2.setInstrument("BCH");
        position2.setPosition(new BigDecimal("100.0"));
        position2.setFund("Fund OSLHK");
        position2.setCustodian("custodian1");

        Position position3 = new Position();
        position3.setPortfolio("Exchange");
        position3.setStrategy("Exchange");
        position3.setInstrument("BCH");
        position3.setPosition(new BigDecimal("1000.0"));
        position3.setFund("Fund OSLHK");
        position3.setCustodian("custodian1");

        Position position4 = new Position();
        position4.setPortfolio("RFQ");
        position4.setStrategy("RFQ");
        position4.setInstrument("BCH");
        position4.setPosition(new BigDecimal("20.0"));
        position4.setFund("Fund OSLSG");
        position4.setCustodian("custodian2");

        PositionMeasurement pm = getPositionMeasurement(position);
        PositionMeasurement pm1 = getPositionMeasurement(position1);
        PositionMeasurement pm2 = getPositionMeasurement(position2);
        PositionMeasurement pm3 = getPositionMeasurement(position3);
        PositionMeasurement pm4 = getPositionMeasurement(position4);

        Mockito.when(dataFeedService.getTransactionDto(position, PositionMeasurement.class)).thenReturn(pm);
        Mockito.when(dataFeedService.getTransactionDto(position1, PositionMeasurement.class)).thenReturn(pm1);
        Mockito.when(dataFeedService.getTransactionDto(position2, PositionMeasurement.class)).thenReturn(pm2);
        Mockito.when(dataFeedService.getTransactionDto(position3, PositionMeasurement.class)).thenReturn(pm3);
        Mockito.when(dataFeedService.getTransactionDto(position4, PositionMeasurement.class)).thenReturn(pm4);

        List<Optional<PositionMeasurement>> measurements = scheduler
                .transformToMeasurements(Arrays.asList(position, position1, position2, position3, position4));

        assertEquals(4, measurements.size());

        List<PositionMeasurement> existedMeasurements = measurements.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        assertEquals(4, existedMeasurements.size());

        List<PositionMeasurement> otcExchangeMeasurements = existedMeasurements.stream()
                .filter(m ->
                        m.getCustodian().equals("custodian1") && m.getFund().equals("Fund OSLHK")
                                && m.getPortfolio().equals(PositionPortfolio.getOTCExchangePortfolio())
                )
                .collect(Collectors.toList());
        assertEquals(1, otcExchangeMeasurements.size());
        PositionMeasurement otcMeasurement = otcExchangeMeasurements.stream().findFirst().orElse(null);
        assertEquals("1100.0", otcMeasurement.getPosition());
        assertEquals("custodian1", otcMeasurement.getCustodian());

        List<PositionMeasurement> rfqMeasurements = existedMeasurements.stream()
                .filter(m ->
                        m.getCustodian().equals("custodian2") && m.getFund().equals("Fund OSLSG")
                                && m.getPortfolio().equals("RFQ")
                )
                .collect(Collectors.toList());
        assertEquals(1, rfqMeasurements.size());
        PositionMeasurement rfqMeasurement = rfqMeasurements.stream().findFirst().orElse(null);
        assertEquals("20.0", rfqMeasurement.getPosition());
        assertEquals("custodian2", rfqMeasurement.getCustodian());
    }

    private PositionMeasurement getPositionMeasurement(Position position) {
        PositionMeasurement pm = new PositionMeasurement();
        pm.setCustodian(position.getCustodian());
        pm.setFund(position.getFund());
        pm.setInstrument(position.getInstrument());
        pm.setStrategy(position.getStrategy());

        return pm;
    }
}
