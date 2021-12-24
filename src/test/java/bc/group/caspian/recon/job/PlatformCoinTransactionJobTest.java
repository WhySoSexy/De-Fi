package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.platform.coinTransaction.PlatformCoinTransaction;
import bc.group.caspian.recon.api.platform.coinTransaction.PlatformCoinTransactionResponse;
import bc.group.caspian.recon.api.platform.coinTransaction.PlatformCoinTransactionResult;
import bc.group.caspian.recon.domain.mysql.SchedulerStatusEntity;
import bc.group.caspian.recon.repository.ScheduledStatusRepository;
import bc.group.caspian.recon.service.DataFeedService;
import bc.group.caspian.recon.service.PlatformCoinTransactionService;
import bc.group.caspian.recon.service.api.platform.PlatformClient;
import bc.group.caspian.recon.service.config.PlatformCoinTransactionProperties;
import org.influxdb.InfluxDB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class PlatformCoinTransactionJobTest {


    @InjectMocks
    PlatformCoinTransactionJob platformCoinTransactionJob;

    @Mock
     DataFeedService dataFeedService;

    @Autowired
     InfluxDB influxDB;

    @Value("${spring.influx.database}")
     String database;

    @Mock
    PlatformClient client;

    @Mock
    ScheduledStatusRepository repository;

    @Mock
    PlatformCoinTransactionService service;

    @Mock
    PlatformCoinTransactionProperties properties;

    @BeforeEach
    public void beforeEach() {
        dataFeedService = new DataFeedService();
        ReflectionTestUtils.setField(dataFeedService, "influxDB" , influxDB);
        ReflectionTestUtils.setField(dataFeedService,"influxDatabase", database);
    }
    @Test
    public void PlatformCoinTxnJobTest() {
        PlatformCoinTransactionResponse response = getPlatformApiResponse();
        Mockito.when(client.getCoinTransactions(any())).thenReturn(response);
        Mockito.when(repository.findSchedulerStatusEntityByName(any())).thenReturn(null);
        Mockito.when(properties.getBatchSize()).thenReturn(10L);
        Mockito.when(properties.getStartDate()).thenReturn(1601510411111L);
        Mockito.when(service.getPlatformCoinTransactions()).thenReturn(response);
        platformCoinTransactionJob.runPlatformTransactionJob();
        SchedulerStatusEntity status = new SchedulerStatusEntity();
        LocalDateTime date = new Timestamp(new Long("1601510400000")).toLocalDateTime();
        status.setFromTs(Timestamp.valueOf(date));
        Mockito.when(repository.findSchedulerStatusEntityByName("COIN_TRANSACTION")).thenReturn(status);
        // FromTs should be updated with last processed Ts value
        assertEquals(status.getFromTs().toInstant().toEpochMilli(),new Long("1601510400000"));

    }

    @Test
    public void PlatformCoinTxnJobTestWithNullResult() {
        PlatformCoinTransactionResponse response = new PlatformCoinTransactionResponse();
        response.setResult(null);
        Mockito.when(client.getCoinTransactions(any())).thenReturn(response);
        Mockito.when(repository.findSchedulerStatusEntityByName(any())).thenReturn(null);
        Mockito.when(properties.getBatchSize()).thenReturn(10L);
        Mockito.when(properties.getStartDate()).thenReturn(1601510400012L);
        Mockito.when(service.getPlatformCoinTransactions()).thenReturn(response);
        platformCoinTransactionJob.runPlatformTransactionJob();
        SchedulerStatusEntity status = new SchedulerStatusEntity();
        LocalDateTime date = new Timestamp(new Long("1601510400012")).toLocalDateTime();
        status.setFromTs(Timestamp.valueOf(date));
        Mockito.when(repository.findSchedulerStatusEntityByName("COIN_TRANSACTION")).thenReturn(status);
        // When result is null from api fromTs should not be updated
        assertEquals(status.getFromTs().toInstant().toEpochMilli(), new Long("1601510400012"));

    }

    private PlatformCoinTransactionResponse getPlatformApiResponse(){
        PlatformCoinTransaction txn = new PlatformCoinTransaction();
        txn.setAmount("10");
        txn.setAccountUuid("89c1dee1-79bd-47bd-a982-ea8d51157fb4");
        txn.setCoinAddress("rU2mEJSLqBRkYLVTv55rFTgQajkLTnT6mA");
        txn.setFee("0.0");
        txn.setId("100");
        txn.setCcy("XRP");
        txn.setProcessedDateTime("1601510400000");
        PlatformCoinTransactionResponse response = new PlatformCoinTransactionResponse();
        PlatformCoinTransactionResult res = new PlatformCoinTransactionResult();
        res.setOffset(0L);
        res.setTotalCount(1L);
        res.setTransactions(Collections.singletonList(txn));
        response.setResultCode("OK");
        response.setTimestamp("1612939977366");
        response.setResult(res);
        return response;
    }
}
