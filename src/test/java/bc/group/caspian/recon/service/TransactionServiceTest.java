package bc.group.caspian.recon.service;

import bc.group.caspian.recon.config.caspian.CaspianConfiguration;
import bc.group.caspian.recon.domain.mysql.TransactionEntity;
import bc.group.caspian.recon.domain.reconInflux.TransactionMeasurement;
import bc.group.caspian.recon.repository.TransactionRepository;
import group.bc.caspian.connector.model.Transaction;
import org.influxdb.dto.Query;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private DataFeedService dataFeedService;

    @Autowired
    private CaspianConfiguration caspianConfiguration;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @Disabled
    public void getTransactionsTest() {
        dataFeedService.getInfluxDB().query(new Query("DROP DATABASE " + dataFeedService.getInfluxDatabase()));
        dataFeedService.getInfluxDB().query(new Query("CREATE DATABASE " + dataFeedService.getInfluxDatabase()));
        List<Transaction>  transactionList = transactionService.getTransactions("Fund OSLHK","T-30", "Now", null);

        assert transactionList != null;

        transactionList.forEach(System.out::println);

    }

    @Test
    @Disabled
    public void getTransactionWithMapTest() {
        dataFeedService.getInfluxDB().query(new Query("DROP DATABASE " + dataFeedService.getInfluxDatabase()));
        dataFeedService.getInfluxDB().query(new Query("CREATE DATABASE " + dataFeedService.getInfluxDatabase()));
        Map<String, String> parmasMap = new HashMap<>();
        caspianConfiguration.caspianReconQueryParameters().forEach(parmasMap::put);
        List<Transaction> transactionList = transactionService.getTransactions(parmasMap, null);

        assert transactionList != null;

        transactionList.forEach(System.out::println);
    }

    @Test
    @Disabled
    public void pushDataToInfluxTest() {
        dataFeedService.getInfluxDB().query(new Query("DROP DATABASE " + dataFeedService.getInfluxDatabase()));
        dataFeedService.getInfluxDB().query(new Query("CREATE DATABASE " + dataFeedService.getInfluxDatabase()));
        List<Transaction>  transactionList = transactionService.getTransactions("Fund OSLHK",
                "2019-Jan-01",
                "Now",
                null,
                "updateTimeStamp",
                "inputTimestamp",
                "additionalData");

        assert transactionList != null;

        transactionList.forEach( t -> {
            TransactionMeasurement tm = dataFeedService.getTransactionDto(t, TransactionMeasurement.class);
            System.out.println(t);
            System.out.println(tm);
            dataFeedService.publishToInflux(tm);
        });

        Query q = new Query("SELECT * FROM transaction", dataFeedService.getInfluxDatabase());
        dataFeedService.getInfluxDB().query(q,1, queryResult -> System.out.println(queryResult.toString()));

    }

    @Test
    @Disabled
    public void pushDataToMysqlTest() {
        dataFeedService.getInfluxDB().query(new Query("DROP DATABASE " + dataFeedService.getInfluxDatabase()));
        dataFeedService.getInfluxDB().query(new Query("CREATE DATABASE " + dataFeedService.getInfluxDatabase()));
        List<Transaction>  transactionList = transactionService.getTransactions("Fund OSLHK","2019-Jan-01", "Now",
                null,"inputTimestamp","updateTimestamp","instrument","currency","additionalData");

        transactionList.forEach(System.out::println);

        assert transactionList != null;

        transactionRepository.deleteAll();

        transactionList.forEach( t ->{
            TransactionEntity transactionEntity = dataFeedService.getTransactionDto(t, TransactionEntity.class);
            transactionRepository.save(transactionEntity);
        });

        transactionRepository.findAll().forEach(System.out::println);

    }

    @Test
    public void assertThatCaspianPathNameIsCorrect() {
        assertEquals("pms-demo", caspianConfiguration.caspianConnectionProperties().getPathName());
    }
}

