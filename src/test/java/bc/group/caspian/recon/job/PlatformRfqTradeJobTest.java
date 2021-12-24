package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.platform.rfqTrades.PlatformRfqTrade;
import bc.group.caspian.recon.domain.mysql.PlatformRfqTradeEntity;
import bc.group.caspian.recon.domain.reconInflux.ClientTradeMeasurement;
import bc.group.caspian.recon.domain.reconInflux.PlatformRfqTradeMeasurement;
import bc.group.caspian.recon.repository.PlatformRfqTradeRepository;
import bc.group.caspian.recon.service.DataFeedService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class PlatformRfqTradeJobTest {

    @InjectMocks
    PlatformRfqTradeJob scheduler;

    @Mock
    PlatformRfqTradeRepository tradeRepository;

    @Mock
    DataFeedService dataFeedService;

    @Mock
    MeterRegistry meterRegistry;

    private PlatformRfqTrade trade;
    private PlatformRfqTrade trade1;

    @BeforeEach
    public void init() {
        trade = new PlatformRfqTrade();
        trade.setTradeId("rfq-trade-0");
        trade.setTradeUuid("62ed1d35-cfcc-4e3b-95f9-1a41437ed17f");
        trade.setDateCreated("2021-04-23T15:07:19.000+08:00");
        trade.setLastUpdated("2021-04-23T16:17:45.000+08:00");
        trade.setBuyTradedCurrency(true);
        trade.setTradedCurrency("BTC");
        trade.setSettlementCurrency("USD");
        trade.setTradedCurrencyAmount("0.00906430");
        trade.setSettlementCurrencyAmount("450.00000000");
        trade.setForUsername("christine.sze+client@osl.com");
        trade.setForUserUuid("828093eb-cf24-42f2-85b3-b2fe3d84d476");
        trade.setSiteGroup("OSLLC_GROUP");
        trade.setTradeState("DVP_PTS_SETTLED");
        trade.setTreasuryUserUuid("7afe7ebc-d096-4be7-9580-2c36f118eb49");
        trade.setTreasuryUsername("prakash.konagi+treasury_ds@osl.com");
        trade.setTreasuryUserSiteGroup("OSLSG_GROUP");
        trade.setEnabledSimpleTradeWL(false);
        trade.setEnabledSimpleTradeWLSegWallet(true);

        trade1 = new PlatformRfqTrade();
        trade1.setTradeId("rfq-trade-1");
        trade1.setDateCreated("2021-04-23T15:07:19.000+08:00");
        trade1.setLastUpdated("2021-04-23T16:17:45.000+08:00");
        trade1.setSiteGroup("OSLLC_GROUP");
    }

    @Test
    public void testProcessIfTradesAreNotExisted() {
        assertEquals(0, scheduler.process(new ArrayList<>()));
    }

    @Test
    public void testProcessIfTradesAreExistedAndWereNeverFetched() {
        Mockito.when(tradeRepository.findFirstByTradeId(any())).thenReturn(null);

        PlatformRfqTradeEntity entity = new PlatformRfqTradeEntity();
        entity.setTradeId("rfq-trade-0");
        PlatformRfqTradeEntity entity1 = new PlatformRfqTradeEntity();
        entity1.setTradeId("rfq-trade-1");

        PlatformRfqTradeMeasurement tm1 = new PlatformRfqTradeMeasurement();
        tm1.setTradeId("rfq-trade-0");
        PlatformRfqTradeMeasurement tm2 = new PlatformRfqTradeMeasurement();
        tm2.setTradeId("retry-feed-1");

        ClientTradeMeasurement cm1 = new ClientTradeMeasurement();
        cm1.setTradeId("rfq-trade-0");
        ClientTradeMeasurement cm2 = new ClientTradeMeasurement();
        cm2.setTradeId("retry-feed-1");

        Mockito.when(dataFeedService.getTransactionDto(trade, PlatformRfqTradeMeasurement.class)).thenReturn(tm1);
        Mockito.when(dataFeedService.getTransactionDto(trade1, PlatformRfqTradeMeasurement.class)).thenReturn(tm2);
        Mockito.when(dataFeedService.getTransactionDto(trade, PlatformRfqTradeEntity.class)).thenReturn(entity);
        Mockito.when(dataFeedService.getTransactionDto(trade1, PlatformRfqTradeEntity.class)).thenReturn(entity1);
        Mockito.when(dataFeedService.getTransactionDto(trade, ClientTradeMeasurement.class)).thenReturn(cm1);
        Mockito.when(dataFeedService.getTransactionDto(trade1, ClientTradeMeasurement.class)).thenReturn(cm2);

        Mockito.doNothing().when(dataFeedService).publishToInflux(any());
        Mockito.when(tradeRepository.saveAll(any())).thenReturn(any());

        List<PlatformRfqTrade> transactionEntities = Arrays.asList(trade, trade1);
        assertEquals(2, scheduler.process(transactionEntities));
    }

    @Test
    public void testProcessIfTradesAreExistedAndWereFetchedButNotCompletedTrade() {
        Mockito.when(tradeRepository.findFirstByTradeId(any())).thenReturn(null);

        PlatformRfqTradeEntity entity = new PlatformRfqTradeEntity();
        entity.setTradeId("rfq-trade-0");
        entity.setId(0L);
        entity.setCompleted(false);
        PlatformRfqTradeEntity entity1 = new PlatformRfqTradeEntity();
        entity1.setTradeId("rfq-trade-1");
        entity1.setId(1l);
        entity1.setCompleted(false);

        PlatformRfqTradeMeasurement tm1 = new PlatformRfqTradeMeasurement();
        tm1.setTradeId("rfq-trade-0");
        PlatformRfqTradeMeasurement tm2 = new PlatformRfqTradeMeasurement();
        tm2.setTradeId("retry-feed-1");
        ClientTradeMeasurement cm1 = new ClientTradeMeasurement();
        cm1.setTradeId("rfq-trade-0");
        ClientTradeMeasurement cm2 = new ClientTradeMeasurement();
        cm2.setTradeId("retry-feed-1");

        Mockito.when(dataFeedService.getTransactionDto(trade, PlatformRfqTradeMeasurement.class)).thenReturn(tm1);
        Mockito.when(dataFeedService.getTransactionDto(trade1, PlatformRfqTradeMeasurement.class)).thenReturn(tm2);
        Mockito.when(dataFeedService.getTransactionDto(trade, PlatformRfqTradeEntity.class)).thenReturn(entity);
        Mockito.when(dataFeedService.getTransactionDto(trade1, PlatformRfqTradeEntity.class)).thenReturn(entity1);
        Mockito.when(dataFeedService.getTransactionDto(trade, ClientTradeMeasurement.class)).thenReturn(cm1);
        Mockito.when(dataFeedService.getTransactionDto(trade1, ClientTradeMeasurement.class)).thenReturn(cm2);

        Mockito.doNothing().when(dataFeedService).publishToInflux(any());
        Mockito.when(tradeRepository.saveAll(any())).thenReturn(any());

        List<PlatformRfqTrade> transactionEntities = Arrays.asList(trade, trade1);
        assertEquals(2, scheduler.process(transactionEntities));
    }

    @Test
    public void testProcessIfPublishToInfluxThrowsExceptions() {
        Mockito.when(tradeRepository.findFirstByTradeId(any())).thenReturn(null);

        PlatformRfqTradeEntity entity = new PlatformRfqTradeEntity();
        entity.setTradeId("rfq-trade-0");
        PlatformRfqTradeEntity entity1 = new PlatformRfqTradeEntity();
        entity1.setTradeId("rfq-trade-1");

        PlatformRfqTradeMeasurement tm1 = new PlatformRfqTradeMeasurement();
        tm1.setTradeId("rfq-trade-0");
        PlatformRfqTradeMeasurement tm2 = new PlatformRfqTradeMeasurement();
        tm2.setTradeId("retry-feed-1");
        ClientTradeMeasurement cm1 = new ClientTradeMeasurement();
        cm1.setTradeId("rfq-trade-0");
        ClientTradeMeasurement cm2 = new ClientTradeMeasurement();
        cm2.setTradeId("retry-feed-1");

        Mockito.when(dataFeedService.getTransactionDto(trade, PlatformRfqTradeMeasurement.class)).thenReturn(tm1);
        Mockito.when(dataFeedService.getTransactionDto(trade1, PlatformRfqTradeMeasurement.class)).thenReturn(tm2);
        Mockito.when(dataFeedService.getTransactionDto(trade, PlatformRfqTradeEntity.class)).thenReturn(entity);
        Mockito.when(dataFeedService.getTransactionDto(trade1, PlatformRfqTradeEntity.class)).thenReturn(entity1);
        Mockito.when(dataFeedService.getTransactionDto(trade, ClientTradeMeasurement.class)).thenReturn(cm1);
        Mockito.when(dataFeedService.getTransactionDto(trade1, ClientTradeMeasurement.class)).thenReturn(cm2);

        Mockito.doNothing().when(dataFeedService).publishToInflux(tm1);
        Mockito.doNothing().when(dataFeedService).publishToInflux(tm2);
        Mockito.doNothing().when(dataFeedService).publishToInflux(cm1);
        Mockito.doNothing().when(dataFeedService).publishToInflux(cm2);

        Mockito.doThrow(new IllegalStateException("Error occurred")).when(dataFeedService).publishToInflux(tm1);
        Mockito.when(tradeRepository.saveAll(any())).thenReturn(any());

        List<PlatformRfqTrade> transactionEntities = Arrays.asList(trade, trade1);
        assertEquals(1, scheduler.process(transactionEntities));
    }
}
