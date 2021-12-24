package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.platform.hedgeTrades.*;
import bc.group.caspian.recon.repository.ScheduledStatusRepository;
import bc.group.caspian.recon.service.api.platform.PlatformClient;
import bc.group.caspian.recon.service.config.PlatformHedgeTradeProperties;
import org.junit.jupiter.api.Disabled;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class PlatformHedgeTradeServiceTest {

    @Mock
    PlatformClient client;

    @Mock
    ScheduledStatusRepository repository;

    @InjectMocks
    PlatformHedgeTradeService service;

    @Mock
    PlatformHedgeTradeProperties properties;

    @Test
    public void getPlatformHedgeTradesTest() {

        PlatformHedgeTrade trade = new PlatformHedgeTrade();
        trade.setBuyTradedCurrency(true);
        trade.setTradedCurrency("BTC");
        trade.setSettlementCurrency("USD");
        trade.setLastUpdated("2021-01-04T12:47:20.000+08:00");
        trade.setId("37939");
        trade.setUuid("e731f33b-c5d5-46ee-9b05-31f19d45c904");

        PlatformHedgeMerchantTransaction merchantTransaction1 = new PlatformHedgeMerchantTransaction();
        merchantTransaction1.setTransactionId("7023253");
        merchantTransaction1.setUserUuid("20e16993-435b-4fd0-a40f-e8a160d4d529");
        merchantTransaction1.setUsername("christine.sze+CP1@osl.com");
        merchantTransaction1.setSiteGroup("OSLLC_GROUP");
        merchantTransaction1.setCcy("USD");
        merchantTransaction1.setAmount(new BigDecimal("134.29000000"));
        merchantTransaction1.setTxnType("TRADE_DEBIT");
        merchantTransaction1.setState("PROCESSED");
        merchantTransaction1.setProcessedDateTime("2021-01-04T12:47:19.492+08:00");
        merchantTransaction1.setReceivedDateTime("2021-01-04T12:47:19.000+08:00");

        PlatformHedgeMerchantTransaction merchantTransaction2 = new PlatformHedgeMerchantTransaction();
        merchantTransaction2.setTransactionId("7023254");
        merchantTransaction2.setUserUuid("20e16993-435b-4fd0-a40f-e8a160d4d529");
        merchantTransaction2.setUsername("christine.sze+CP1@osl.com");
        merchantTransaction2.setSiteGroup("OSLLC_GROUP");
        merchantTransaction2.setCcy("BTC");
        merchantTransaction2.setAmount(new BigDecimal("0.00400000"));
        merchantTransaction2.setTxnType("TRADE_CREDIT");
        merchantTransaction2.setState("PROCESSED");
        merchantTransaction2.setProcessedDateTime("2021-01-04T12:47:19.492+08:00");
        merchantTransaction2.setReceivedDateTime("2021-01-04T12:47:19.000+08:00");

        PlatformHedgeFloatTransaction floatTransaction1 = new PlatformHedgeFloatTransaction();
        floatTransaction1.setTransactionId("7023255");
        floatTransaction1.setUserUuid("20e16993-435b-4fd0-a40f-e8a160d4d529");
        floatTransaction1.setUsername("christine.sze+CP1@osl.com");
        floatTransaction1.setSiteGroup("OSLLC_GROUP");
        floatTransaction1.setCcy("USD");
        floatTransaction1.setAmount(new BigDecimal("0.00400000"));
        floatTransaction1.setTxnType("TRADE_CREDIT");
        floatTransaction1.setState("PROCESSED");
        floatTransaction1.setProcessedDateTime("2021-01-04T12:47:19.492+08:00");
        floatTransaction1.setReceivedDateTime("2021-01-04T12:47:19.000+08:00");
        floatTransaction1.setOriginatingTransactionId("7023253");

        PlatformHedgeFloatTransaction floatTransaction2 = new PlatformHedgeFloatTransaction();
        floatTransaction2.setTransactionId("7023256");
        floatTransaction2.setUserUuid("20e16993-435b-4fd0-a40f-e8a160d4d529");
        floatTransaction2.setUsername("christine.sze+CP1@osl.com");
        floatTransaction2.setSiteGroup("OSLLC_GROUP");
        floatTransaction2.setCcy("BTC");
        floatTransaction2.setAmount(new BigDecimal("0.00400000"));
        floatTransaction2.setTxnType("TRADE_CREDIT");
        floatTransaction2.setState("PROCESSED");
        floatTransaction2.setProcessedDateTime("2021-01-04T12:47:19.492+08:00");
        floatTransaction2.setReceivedDateTime("2021-01-04T12:47:19.000+08:00");
        floatTransaction2.setOriginatingTransactionId("7023253");

        trade.setMerchantTransactions(Arrays.asList(merchantTransaction1, merchantTransaction2));
        trade.setFloatTransactions(Arrays.asList(floatTransaction1, floatTransaction2));

        PlatformHedgeTradeResponse response = new PlatformHedgeTradeResponse();
        PlatformHedgeTradeResult res = new PlatformHedgeTradeResult();
        res.setOffset(0L);
        res.setTotalCount(1L);
        res.setTrades(Collections.singletonList(trade));
        response.setResultCode("OK");
        response.setTimestamp("1612939977366");
        response.setResult(res);

        Mockito.when(client.getHedgeTrades(any())).thenReturn(response);
        Mockito.when(repository.findSchedulerStatusEntityByName(any())).thenReturn(null);
        Mockito.when(properties.getBatchSize()).thenReturn("20");
        Mockito.when(properties.getStartDate()).thenReturn(1601510400000L);
        PlatformHedgeTradeResponse result = service.getPlatformHedgeTrades();
        PlatformHedgeTrade hedgeTrade = result.getResult().getTrades().get(0);

        assertEquals(trade.getBuyTradedCurrency(), hedgeTrade.getBuyTradedCurrency());
        assertEquals(trade.getId(), hedgeTrade.getId());
        assertEquals(trade.getFloatTransactions().size(), hedgeTrade.getFloatTransactions().size());
        assertEquals(trade.getUuid(), hedgeTrade.getUuid());
        assertEquals(trade.getTradedCurrency(), hedgeTrade.getTradedCurrency());
        assertEquals(trade.getSettlementCurrency(), hedgeTrade.getSettlementCurrency());
        assertEquals(trade.getMerchantTransactions().size(), hedgeTrade.getMerchantTransactions().size());
        assertEquals(trade.getLastUpdated(), hedgeTrade.getLastUpdated());
    }
}
