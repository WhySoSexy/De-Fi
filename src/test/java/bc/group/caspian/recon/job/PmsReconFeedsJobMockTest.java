package bc.group.caspian.recon.job;

import bc.group.caspian.recon.config.caspian.CaspianConfiguration;
import bc.group.caspian.recon.domain.mysql.TransactionEntity;
import bc.group.caspian.recon.domain.mysql.TransactionEntityPK;
import bc.group.caspian.recon.domain.reconInflux.TransactionMeasurement;
import bc.group.caspian.recon.repository.TransactionRepository;
import bc.group.caspian.recon.service.DataFeedService;
import bc.group.caspian.recon.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import group.bc.caspian.connector.model.Transaction;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")

public class PmsReconFeedsJobMockTest {

    private final static Logger logger = LoggerFactory.getLogger(PmsReconFeedsJobTest.class);

    @Mock
    private TransactionService transactionService;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private CaspianConfiguration caspianConfiguration;

    @Mock
    private DataFeedService dataFeedService;

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    Counter transactionsCounter;

    @Mock
    Counter pushedReconCounter;

    @InjectMocks
    private PmsReconFeedsJob reconFeedsJob;

    @InjectMocks
    private TransactionEntity transactionEntityMock = new TransactionEntity();

    @InjectMocks
    private TransactionMeasurement transactionMeasurementMock = new TransactionMeasurement();

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIgnoreRFQTradesFromCaspian() {

        List<Transaction> transactionsFromApi = getTransactionsFromApi();
        List<TransactionEntity> transactionsFromDb = getTransactionsFromDb(transactionsFromApi);
        transactionsFromDb.forEach(System.out::println);

        ReflectionTestUtils.setField(reconFeedsJob, "fundIgnoreList", Arrays.asList("Fund OSLSG", "Fund OSLDS", "Fund OSLAM"));
        ReflectionTestUtils.setField(reconFeedsJob, "portfolioIgnoreList", Collections.singletonList("RFQ"));
        ReflectionTestUtils.setField(reconFeedsJob, "strategyIgnoreList", Collections.singletonList("RFQ"));
        ReflectionTestUtils.setField(reconFeedsJob, "startDay", "2020-Aug-10");
        ReflectionTestUtils.setField(reconFeedsJob, "startDayBackwardMin", 15);
        ReflectionTestUtils.setField(reconFeedsJob, "apiTimeIntervalInMinutes", 30);

        TransactionEntity latestUpdateTimestamp = transactionsFromDb.get(0);

        Mockito.when(transactionRepository.findTopByInsertedOrderByUpdateTimestampDesc(true)).thenReturn(Optional.of(latestUpdateTimestamp));

        Mockito.when(transactionService.getTransactions(Mockito.anyMap(), Mockito.isNull())).thenReturn(transactionsFromApi);

        Mockito.when(dataFeedService.getTransactionDto(Mockito.any(), Mockito.eq(TransactionMeasurement.class))).thenReturn(new TransactionMeasurement());

        Mockito.when(dataFeedService.getTransactionDto(Mockito.any(Transaction.class), Mockito.eq(TransactionEntity.class)))
                .thenReturn(transactionsFromDb.get(0), transactionsFromDb.get(1), transactionsFromDb.get(2), transactionsFromDb.get(3));

        List<TransactionEntityPK> pks = getPks(transactionsFromApi);

        logger.info(pks.toString());

        Mockito.when(transactionRepository.existsById(Mockito.any())).thenReturn(true,false, true, true, true, false, true, true);

        Mockito.when(transactionRepository.findById(Mockito.any(TransactionEntityPK.class))).thenReturn(Optional.of(transactionsFromDb.get(3)));

        reconFeedsJob.runReconFeedsJob();

        Mockito.verify(dataFeedService, Mockito.times(3)).publishToInflux(Mockito.any());
        Mockito.verify(transactionRepository, Mockito.times(5)).save(Mockito.any());
    }

    @Test
    public void testGetErrorDescriptionShouldReturnErrorMessage() throws IOException {
        byte[] body = "{\"error\":\"Entity is not supported\"}".getBytes();
        Mockito.when(caspianConfiguration.objectMapper()).thenReturn(new ObjectMapper());

        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid params", body, null);
        String error = reconFeedsJob.getErrorDescription(exception);
        assertEquals(error, "Entity is not supported");
    }

    @Test
    public void testGetErrorDescriptionShouldReturnNull() throws IOException {
        byte[] body = "{}".getBytes();
        Mockito.when(caspianConfiguration.objectMapper()).thenReturn(new ObjectMapper());

        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid params", body, null);
        assertNull(reconFeedsJob.getErrorDescription(exception));
    }

    @Test
    public void testGetErrorDescriptionShouldThrows() {
        Mockito.when(caspianConfiguration.objectMapper()).thenReturn(new ObjectMapper());

        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        assertThrows(IOException.class, () -> {
            reconFeedsJob.getErrorDescription(exception);
        });
    }
    @Test
    public void testChangeTransactionEntityTradedAndSettleAndCurrencyCcy(){

       // List<Transaction> transactionsFromApi = getTransactionsFromApi();
      //  List<TransactionEntity> transactionsEntityFromDb = getTransactionsFromDb(transactionsFromApi);
        //TransactionEntity transactionEntity = new TransactionEntity();
       // reconFeedsJob.changeTransactionEntityTradedAndSettleAndCurrencyCcy(transactionsEntityFromDb.forEach(transactionEntity -> transactionEntityMock.getCurrency()));
                //.thenReturn(transactionsFromDb.getCurrency());

    }
    @Test
    public void testChangeTransactionMeasurementTradedAndSettleAndCurrencyCcy() {

    }

    @Test
    public  void testCheckTransactionEntityCurrencyTradedAndSettleCcy() {

        TransactionEntity transactionEntityMock = Mockito.mock(TransactionEntity.class);
        TransactionEntity transactionEntitySpy = Mockito.spy(TransactionEntity.class);
        TransactionEntity transactionEntity = new TransactionEntity();
        //Mockito.when(transactionEntityMock.getCurrency())
              //  .thenReturn("");
        //Assertions.assertEquals("BAND", transactionEntityMock.getCurrency());
        transactionEntity.setCurrency("BNP");
        Assertions.assertEquals("BAND",transactionEntity.getCurrency());
        transactionEntity.setCurrency("DIN");
        Assertions.assertEquals("DAI",transactionEntity.getCurrency());
        transactionEntity.setCurrency("null");
        Assertions.assertEquals("null",transactionEntity.getCurrency());
        transactionEntity.setCurrency("USD");
        Assertions.assertEquals("USD",transactionEntity.getCurrency());
        transactionEntity.setCurrency("BTC");
        Assertions.assertEquals("BTC",transactionEntity.getCurrency());
        transactionEntity.setTradedCcy("BNP");
        Assertions.assertEquals("BAND",transactionEntity.getTradedCcy());
        transactionEntity.setTradedCcy("DIN");
        Assertions.assertEquals("DAI",transactionEntity.getTradedCcy());
        transactionEntity.setTradedCcy("null");
        Assertions.assertEquals("null",transactionEntity.getTradedCcy());
        transactionEntity.setTradedCcy("USD");
        Assertions.assertEquals("USD",transactionEntity.getTradedCcy());
        transactionEntity.setTradedCcy("BTC");
        Assertions.assertEquals("BTC",transactionEntity.getTradedCcy());
        transactionEntity.setSettleCcy("DIN");
        Assertions.assertEquals("DAI",transactionEntity.getSettleCcy());
        transactionEntity.setSettleCcy("BNP");
        Assertions.assertEquals("BAND",transactionEntity.getSettleCcy());
        transactionEntity.setSettleCcy("null");
        Assertions.assertEquals("null",transactionEntity.getSettleCcy());
        transactionEntity.setSettleCcy("USD");
        Assertions.assertEquals("USD",transactionEntity.getSettleCcy());
        transactionEntity.setSettleCcy("BTC");
        Assertions.assertEquals("BTC",transactionEntity.getSettleCcy());
    }
    @Test
    public void testCheckTransactionMeasurementCurrencyTradedAndSettleCcy() {
        TransactionMeasurement transactionMeasurement = new TransactionMeasurement();
        transactionMeasurement.setCurrency("BNP");
        Assertions.assertEquals("BAND",transactionMeasurement.getCurrency());
        transactionMeasurement.setCurrency("DIN");
        Assertions.assertEquals("DAI",transactionMeasurement.getCurrency());
        transactionMeasurement.setCurrency("null");
        Assertions.assertEquals("null",transactionMeasurement.getCurrency());
        transactionMeasurement.setCurrency("USD");
        Assertions.assertEquals("USD",transactionMeasurement.getCurrency());
        transactionMeasurement.setCurrency("BTC");
        Assertions.assertEquals("BTC",transactionMeasurement.getCurrency());
        transactionMeasurement.setTradedCcy("BNP");
        Assertions.assertEquals("BAND",transactionMeasurement.getTradedCcy());
        transactionMeasurement.setTradedCcy("DIN");
        Assertions.assertEquals("DAI",transactionMeasurement.getTradedCcy());
        transactionMeasurement.setTradedCcy("null");
        Assertions.assertEquals("null",transactionMeasurement.getTradedCcy());
        transactionMeasurement.setTradedCcy("USD");
        Assertions.assertEquals("USD",transactionMeasurement.getTradedCcy());
        transactionMeasurement.setTradedCcy("BTC");
        Assertions.assertEquals("BTC",transactionMeasurement.getTradedCcy());
        transactionMeasurement.setSettleCcy("DIN");
        Assertions.assertEquals("DAI",transactionMeasurement.getSettleCcy());
        transactionMeasurement.setSettleCcy("BNP");
        Assertions.assertEquals("BAND",transactionMeasurement.getSettleCcy());
        transactionMeasurement.setSettleCcy("null");
        Assertions.assertEquals("null",transactionMeasurement.getSettleCcy());
        transactionMeasurement.setSettleCcy("USD");
        Assertions.assertEquals("USD",transactionMeasurement.getSettleCcy());
        transactionMeasurement.setSettleCcy("BTC");
        Assertions.assertEquals("BTC",transactionMeasurement.getSettleCcy());
    }

    private List<Transaction> getTransactionsFromApi() {

        return new ArrayList<>(Arrays.asList(
                buildTransaction("1234", "BTC", "Fund OSLSG", "RFQ", "RFQ"),
                buildTransaction("4321", "BTC", "Fund OSLDS", "RFQ", "RFQ"),
                buildTransaction("543", "BTC", "Fund OSLAM", "RFQ", "RFQ"),
                buildTransaction("876", "BTC", "Fund OSLAM", "HEDGE", "RFQ")
        ));
    }

    private List<TransactionEntity> getTransactionsFromDb(List<Transaction> transactions) {

        return transactions.stream()
                .map(t -> buildTransactionEntity(t.getId(), t.getCashEntry(), t.getFund(), t.getPortfolio(), t.getStrategy()))
                .collect(Collectors.toList());
    }

    private Transaction buildTransaction(String id, String cashEntry, String fund, String portfolio, String strategy) {

        Transaction transaction = new Transaction();

        transaction.setId(id);
        transaction.setCashEntry(cashEntry);
        transaction.setFund(fund);
        transaction.setPortfolio(portfolio);
        transaction.setStrategy(strategy);

        return transaction;
    }

    private TransactionEntity buildTransactionEntity(String id, String cashEntry, String fund, String portfolio, String strategy) {

        TransactionEntity transactionEntity = new TransactionEntity();

        transactionEntity.setId(id);
        transactionEntity.setCashEntry(cashEntry);
        transactionEntity.setFund(fund);
        transactionEntity.setPortfolio(portfolio);
        transactionEntity.setStrategy(strategy);
        transactionEntity.setUpdateTimestamp(ZonedDateTime.now().plusMinutes(45));

        return transactionEntity;
    }

    private List<TransactionEntityPK> getPks(List<Transaction> transactions) {
        return transactions.stream()
                .map(t -> new TransactionEntityPK(t.getId(), t.getCashEntry()))
                .collect(Collectors.toList());
    }
}
