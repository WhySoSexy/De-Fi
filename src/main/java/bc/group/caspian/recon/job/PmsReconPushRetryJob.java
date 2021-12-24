package bc.group.caspian.recon.job;

import bc.group.caspian.recon.domain.mysql.TransactionEntity;
import bc.group.caspian.recon.domain.reconInflux.TransactionMeasurement;
import bc.group.caspian.recon.repository.TransactionRepository;
import bc.group.caspian.recon.service.DataFeedService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Profile("!test")
@Service
public class PmsReconPushRetryJob {

    private final DataFeedService dataFeedService;
    private final TransactionRepository transactionRepository;

    private final static Logger logger = LoggerFactory.getLogger(PmsReconPushRetryJob.class);

    @Value("${recon-feeds-push-retry.job-enabled}")
    private boolean pushRetryJobEnabled;

    Counter transactionsCounter;
    Counter pushedReconCounter;

    public PmsReconPushRetryJob(DataFeedService dataFeedService, TransactionRepository transactionRepository, MeterRegistry meterRegistry) {
        this.dataFeedService = dataFeedService;
        this.transactionRepository = transactionRepository;

        String GAUGE_METRICS = "gauge.metrics";

        transactionsCounter = Counter.builder(GAUGE_METRICS)
                .tags("type","transactionSize")
                .description("The number of not inserted transaction grabbed from caspian api")
                .register(meterRegistry);

        pushedReconCounter = Counter.builder(GAUGE_METRICS)
                .tags("type","repushReconSize")
                .description("The number of transaction push into influxdb")
                .register(meterRegistry);
    }

    public void runPushRetryJob() {
        List<TransactionEntity> notInsertedTransactions = transactionRepository.findByInserted(false);
        logger.info("Got {} Not Inserted Transactions from Caspian PMS", notInsertedTransactions.size());
        transactionsCounter.increment(notInsertedTransactions.size());

        AtomicInteger noOfDataPushedToInflux = new AtomicInteger();

        notInsertedTransactions.forEach(t -> {
            TransactionMeasurement tm = dataFeedService.getTransactionDto(t, TransactionMeasurement.class);
            logger.trace("transaction: " + t.toString());
            logger.trace("transaction measurement: " + tm.toString());

            // Push data to influx and add to mysql
            try {
                dataFeedService.publishToInflux(tm);
                t.setInserted(true);
                transactionRepository.save(t);
                noOfDataPushedToInflux.getAndIncrement();
            } catch (Exception e) {
                logger.error("Failed to re - push transaction {} into influx", tm.getCptyCode(), e);
            }

            logger.info("Completed re-pushing {} cash transaction to influx from Caspian PMS ", noOfDataPushedToInflux.intValue());
            pushedReconCounter.increment(noOfDataPushedToInflux.intValue());
        });
    }
}
