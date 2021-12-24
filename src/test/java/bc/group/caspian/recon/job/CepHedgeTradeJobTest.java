package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.cep.CepHedgeTrade;
import bc.group.caspian.recon.api.cep.CepHedgeTradeResponse;
import bc.group.caspian.recon.domain.mysql.CepHedgeTradeEntity;
import bc.group.caspian.recon.repository.CepHedgeTradeRepository;
import bc.group.caspian.recon.service.config.CepHedgeTradeProperties;
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
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class CepHedgeTradeJobTest {

    @InjectMocks
    private CepHedgeTradeJob scheduler;

    @Mock
    private CepHedgeTradeRepository repository;

    @Mock
    private CepHedgeTradeProperties properties;

    @Mock
    MeterRegistry meterRegistry;

    @Test
    public void getIdWhenEntityIsNotNullTest() {
        CepHedgeTradeEntity entity = CepHedgeTradeEntity.builder()
                .id(100L)
                .completed(true)
                .existing(true)
                .build();

        Mockito.when(repository.findTopByOrderByIdDesc()).thenReturn(Optional.of(entity));
        Long id = scheduler.getId();

        assertEquals(101, id);
    }

    @Test
    public void getIdWhenEntityIsNullTest() {
        Optional<CepHedgeTradeEntity> entity = Optional.ofNullable(null);
        Mockito.when(repository.findTopByOrderByIdDesc()).thenReturn(entity);
        Mockito.when(properties.getStartId()).thenReturn(1L);

        Long id = scheduler.getId();

        assertEquals(1, id);
    }

    @Test
    public void updateIdsForHedgedClientTradesTest() {
        assertEquals(1, scheduler.updateIds(getHedgedResponse()));
    }

    @Test
    public void updateIdsForClosedClientTradesWithHedgedTradesTest() {
        assertEquals(1, scheduler.updateIds(getClosedResponse()));
    }

    @Test
    public void updateIdsForClosedClientTradesWithoutHedgedTradesTest() {
        CepHedgeTradeResponse response = getClosedResponse();
        response.setRfqTradeFillList(Collections.emptyList());
        assertEquals(1, scheduler.updateIds(getClosedResponse()));
    }

    @Test
    public void isCompletedTest() {
        Mockito.when(properties.getValid()).thenReturn(Arrays.asList("CLOSED", "HEDGED"));
        assertEquals(true, scheduler.isCompleted(getHedgedResponse().getRfqTradeList().get(0)));
    }

    @Test
    public void isCompletedClosedClientTradesWithHedgedTradesTest() {
        Mockito.when(properties.getValid()).thenReturn(Arrays.asList("CLOSED", "HEDGED"));
        assertEquals(true, scheduler.isCompleted(getClosedResponse().getRfqTradeList().get(0)));
    }

    @Test
    public void isCompletedClosedClientTradesWithoutHedgedTradesTest() {
        Mockito.when(properties.getValid()).thenReturn(Arrays.asList("CLOSED", "HEDGED"));
        assertEquals(true, scheduler.isCompleted(getClosedResponse().getRfqTradeList().get(0)));
    }

    private CepHedgeTradeResponse getHedgedResponse() {
        CepHedgeTrade client = new CepHedgeTrade();
        CepHedgeTrade hedge1 = new CepHedgeTrade();
        CepHedgeTrade hedge2 = new CepHedgeTrade();

        client.setId("440");
        client.setSettlementCurrencyAmount(new BigDecimal("3076.29000000"));
        client.setTradedCurrency("ETH");
        client.setStatus("HEDGED");
        client.setQuoteProvider("cc67ba0c-c229-4e1e-a537-a1790fce3ba6");
        client.setCustomer("");
        client.setSettlementCurrency("HKD");
        client.setSiteGroup("OSL_GROUP");
        client.setOriginTradeId("10021");
        client.setCcyPair("ETHUSD");
        client.setTradedCurrencyAmount(new BigDecimal("2.00000000"));
        client.setDateCreated(new BigDecimal("1536548145000"));
        client.setUserBuy(false);


        hedge1.setId("440");
        hedge1.setSettlementCurrencyAmount(new BigDecimal("41265.7763929950000000"));
        hedge1.setTradedCurrency("ETH");
        hedge1.setStatus("HEDGED");
        hedge1.setLastUpdated(new BigDecimal("1549939034000"));
        hedge1.setVenue("JUMP");
        hedge1.setSettlementCurrency("USD");
        hedge1.setFillId("57377393");
        hedge1.setPrice(new BigDecimal("212.10000000"));
        hedge1.setSiteGroup("OSL_GROUP");
        hedge1.setOriginTradeId("10021");
        hedge1.setCcyPair("ETHUSD");
        hedge1.setTradedCurrencyAmount(new BigDecimal("194.55811595"));
        hedge1.setHedgeBuy(false);

        hedge2.setId("440");
        hedge2.setSettlementCurrencyAmount(new BigDecimal("41265.7763929950000000"));
        hedge2.setTradedCurrency("ETH");
        hedge2.setStatus("HEDGED");
        hedge2.setLastUpdated(new BigDecimal("1549939034000"));
        hedge2.setVenue("JUMP");
        hedge2.setSettlementCurrency("USD");
        hedge2.setFillId("57377394");
        hedge2.setPrice(new BigDecimal("213.10000000"));
        hedge2.setSiteGroup("OSL_GROUP");
        hedge2.setOriginTradeId("10021");
        hedge2.setCcyPair("ETHUSD");
        hedge2.setTradedCurrencyAmount(new BigDecimal("194.55811595"));
        hedge2.setHedgeBuy(false);

        CepHedgeTradeResponse response = new CepHedgeTradeResponse();
        response.setRfqTradeList(Collections.singletonList(client));
        response.setRfqTradeFillList(Arrays.asList(hedge1, hedge2));
        response.setLatestId("1000");

        return response;
    }

    private CepHedgeTradeResponse getClosedResponse() {
        CepHedgeTrade client = new CepHedgeTrade();
        CepHedgeTrade hedge1 = new CepHedgeTrade();
        CepHedgeTrade hedge2 = new CepHedgeTrade();

        client.setId("440");
        client.setSettlementCurrencyAmount(new BigDecimal("3076.29000000"));
        client.setTradedCurrency("ETH");
        client.setStatus("CLOSED");
        client.setQuoteProvider("cc67ba0c-c229-4e1e-a537-a1790fce3ba6");
        client.setCustomer("");
        client.setSettlementCurrency("HKD");
        client.setSiteGroup("OSL_GROUP");
        client.setOriginTradeId("10021");
        client.setCcyPair("ETHUSD");
        client.setTradedCurrencyAmount(new BigDecimal("2.00000000"));
        client.setDateCreated(new BigDecimal("1536548145000"));
        client.setUserBuy(false);


        hedge1.setId("440");
        hedge1.setSettlementCurrencyAmount(new BigDecimal("41265.7763929950000000"));
        hedge1.setTradedCurrency("ETH");
        hedge1.setStatus("CLOSED");
        hedge1.setLastUpdated(new BigDecimal("1549939034000"));
        hedge1.setVenue("JUMP");
        hedge1.setSettlementCurrency("USD");
        hedge1.setFillId("57377393");
        hedge1.setPrice(new BigDecimal("212.10000000"));
        hedge1.setSiteGroup("OSL_GROUP");
        hedge1.setOriginTradeId("10021");
        hedge1.setCcyPair("ETHUSD");
        hedge1.setTradedCurrencyAmount(new BigDecimal("194.55811595"));
        hedge1.setHedgeBuy(false);

        hedge2.setId("440");
        hedge2.setSettlementCurrencyAmount(new BigDecimal("41265.7763929950000000"));
        hedge2.setTradedCurrency("ETH");
        hedge2.setStatus("CLOSED");
        hedge2.setLastUpdated(new BigDecimal("1549939034000"));
        hedge2.setVenue("JUMP");
        hedge2.setSettlementCurrency("USD");
        hedge2.setFillId("57377394");
        hedge2.setPrice(new BigDecimal("213.10000000"));
        hedge2.setSiteGroup("OSL_GROUP");
        hedge2.setOriginTradeId("10021");
        hedge2.setCcyPair("ETHUSD");
        hedge2.setTradedCurrencyAmount(new BigDecimal("194.55811595"));
        hedge2.setHedgeBuy(false);

        CepHedgeTradeResponse response = new CepHedgeTradeResponse();
        response.setRfqTradeList(Collections.singletonList(client));
        response.setRfqTradeFillList(Arrays.asList(hedge1, hedge2));
        response.setLatestId("1000");

        return response;
    }
}
