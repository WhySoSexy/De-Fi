package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.platform.hedgeTrades.*;
import bc.group.caspian.recon.domain.mysql.SchedulerStatusEntity;
import bc.group.caspian.recon.repository.PlatformHedgeTradeRepository;
import bc.group.caspian.recon.service.config.PlatformHedgeTradeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class PlatformHedgeTradeJobTest {

    @InjectMocks
    private PlatformHedgeTradeJob scheduler;

    @Mock
    private PlatformHedgeTradeRepository repository;

    @Mock
    private PlatformHedgeTradeProperties properties;

    PlatformHedgeTrade trade;
    PlatformHedgeTradeResponse response;

    @BeforeEach
    public void init() {
        trade = new PlatformHedgeTrade();
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

        PlatformHedgeTradeResult res = new PlatformHedgeTradeResult();
        res.setOffset(0L);
        res.setTotalCount(1L);
        res.setTrades(Collections.singletonList(trade));
        response = new PlatformHedgeTradeResponse();
        response.setResultCode("OK");
        response.setTimestamp("1612939977366");
        response.setResult(res);
    }

    @Test
    public void getValidTradesTest() {
        List<PlatformHedgeTrade> result = scheduler.getValidTrades(Collections.singletonList(trade));

        assertEquals(1, result.size());
    }

    @Test
    public void processStatusTest() {
        Timestamp from = new Timestamp(1618891200000L);
        Timestamp to = new Timestamp(1619308800000L);
        SchedulerStatusEntity status = new SchedulerStatusEntity(1L, 0L, "test", from, to);
        SchedulerStatusEntity result = scheduler.processStatus(status, response);

        assertEquals(0L, result.getOffset());
        assertEquals(1619308790000L, result.getFromTs().getTime());
        assertEquals(1619740800000L, result.getToTs().getTime());
    }

    @Test
    public void getStatusWhenTotalCountEqualsResponseTradeCountTest() {
        SchedulerStatusEntity status = scheduler.getStatus(response);

        assertEquals(0L, status.getOffset());
    }

    @Test
    public void getStatusWhenTotalCountNotEqualsResponseTradeCountTest() {
        response.getResult().setTotalCount(100L);
        SchedulerStatusEntity status = scheduler.getStatus(response);

        assertEquals(1L, status.getOffset());
    }

    @Test
    public void setOffsetZeroAndUpdateTsTest() {
        Timestamp from = new Timestamp(1618891200000L);
        Timestamp to = new Timestamp(1619308800000L);
        SchedulerStatusEntity status = new SchedulerStatusEntity(1L, 30L, "test", from, to);
        scheduler.setOffsetZeroAndUpdateTs(status);

        assertEquals(0L, status.getOffset());
        assertEquals(1619308790000L, status.getFromTs().getTime());
        assertEquals(1619740800000L, status.getToTs().getTime());
    }

    @Test
    public void updateOffsetTest() {
        Timestamp from = new Timestamp(1618891200000L);
        Timestamp to = new Timestamp(1619308800000L);
        SchedulerStatusEntity status = new SchedulerStatusEntity(1L, 0L, "test", from, to);
        scheduler.updateOffset(status, response);

        assertEquals(1L, status.getOffset());
    }

    @Test
    public void isCompletedTrueTest() {
        Timestamp from = new Timestamp(1618891200000L);
        Timestamp to = new Timestamp(1619308800000L);
        SchedulerStatusEntity status = new SchedulerStatusEntity(1L, 0L, "test", from, to);

        assertTrue(scheduler.isCompleted(response, status));
    }

    @Test
    public void isCompletedFalseTest() {
        response.getResult().setTotalCount(100L);
        Timestamp from = new Timestamp(1618891200000L);
        Timestamp to = new Timestamp(1619308800000L);
        SchedulerStatusEntity status = new SchedulerStatusEntity(1L, 0L, "test", from, to);

        assertFalse(scheduler.isCompleted(response, status));
    }

    @Test
    public void getDateWithBatchTest() {
        Timestamp result = scheduler.getDateWithBatch(1619308800000L);

        assertEquals(1619740800000L, result.getTime());
    }

    @Test
    public void getDateMinusSecondsTest() {
        Timestamp result = scheduler.getDateMinusSeconds(1619308800000L);

        assertEquals(1619308790000L, result.getTime());
    }

    @Test
    public void getOffsetTest() {
        assertEquals(1L, scheduler.getOffset(response));
    }

    @Test
    public void getOffsetAndListSizeSum() {
        response.getResult().setTotalCount(100L);
        Timestamp from = new Timestamp(1618891200000L);
        Timestamp to = new Timestamp(1619308800000L);
        SchedulerStatusEntity status = new SchedulerStatusEntity(1L, 0L, "test", from, to);

        assertEquals(1L, scheduler.getOffsetAndListSizeSum(status, response));
    }
}
