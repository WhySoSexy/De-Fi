package bc.group.caspian.recon.job;

import bc.group.caspian.recon.repository.TransactionRepository;
import bc.group.caspian.recon.service.DataFeedService;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.influxdb.dto.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class PmsReconFeedsJobTest {

    private final static Logger logger = LoggerFactory.getLogger(PmsReconFeedsJobTest.class);

    @Autowired
    private DataFeedService dataFeedService;


    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private PmsReconFeedsJob pmsReconFeedsJob;

    @BeforeEach
    public void beforeEach() {
        dataFeedService.getInfluxDB().query(new Query("DROP DATABASE " + dataFeedService.getInfluxDatabase()));
        dataFeedService.getInfluxDB().query(new Query("CREATE DATABASE " + dataFeedService.getInfluxDatabase()));
    }

    @Test
    @Ignore
    @Disabled
    public void ReconFeedsJobTest() {
        pmsReconFeedsJob.runReconFeedsJob();
        Query q = new Query("SELECT * FROM transaction", dataFeedService.getInfluxDatabase());
        dataFeedService.getInfluxDB().query(q,1, queryResult -> System.out.println(queryResult.toString()));
        transactionRepository.findByInserted(true).forEach(transactionEntity -> { logger.info(transactionEntity.toString());});
        logger.info(String.valueOf(transactionRepository.findByInserted(false).size()));
    }
}
