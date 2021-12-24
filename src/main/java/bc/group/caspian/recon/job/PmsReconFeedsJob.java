package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.caspian.CaspianRestClientException;
import bc.group.caspian.recon.config.caspian.CaspianConfiguration;
import bc.group.caspian.recon.domain.mysql.TransactionEntity;
import bc.group.caspian.recon.domain.mysql.TransactionEntityPK;
import bc.group.caspian.recon.domain.reconInflux.TransactionMeasurement;
import bc.group.caspian.recon.repository.TransactionRepository;
import bc.group.caspian.recon.service.DataFeedService;
import bc.group.caspian.recon.service.TransactionService;
import group.bc.caspian.connector.model.Transaction;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Profile("!test")
@Service
public class PmsReconFeedsJob {

    private final static Logger logger = LoggerFactory.getLogger(PmsReconFeedsJob.class);

    private MeterRegistry meterRegistry;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CaspianConfiguration caspianConfiguration;

    @Autowired
    private DataFeedService dataFeedService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Value("${recon-feeds.start-day}")
    private String startDay;

    @Value("${recon-feeds.apiTimeIntervalInMinutes:30}")
    private long apiTimeIntervalInMinutes; //minutes

    private String GAUGE_METRICS = "gauge.metrics";
    Counter transactionsCounter;
    Counter pushedReconCounter;
    Counter errorCounter;
    private int firstTimeRun = 1;

    @Value("${recon-feeds.start-day-backward-min:15}")
    private int startDayBackwardMin;

    @Value("${recon-feeds.ignore-update.fund}")
    private List<String> fundIgnoreList;

    @Value("${recon-feeds.ignore-update.portfolio}")
    private List<String> portfolioIgnoreList;

    @Value("${recon-feeds.ignore-update.strategy}")
    private List<String> strategyIgnoreList;

    public PmsReconFeedsJob(MeterRegistry meterRegistry)
    {
        transactionsCounter = Counter.builder(GAUGE_METRICS)
                .tags("type","transactionSize")
                .description("The number of transaction grab from caspian api")
                .register(meterRegistry);

        pushedReconCounter = Counter.builder(GAUGE_METRICS)
                .tags("type","pushedReconSize")
                .description("The number of transaction pushed into influxdb")
                .register(meterRegistry);

        errorCounter = Counter.builder(GAUGE_METRICS)
                .tags("type","error")
                .description("The number of error")
                .register(meterRegistry);
    }

    public void runReconFeedsJob() throws HttpClientErrorException {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        LocalDateTime now =  LocalDateTime.now(ZoneId.of("UTC"));
        // check whether the lastest updatetimestamp exists or not
        Optional<TransactionEntity> latestUpdateTimestamp = transactionRepository.findTopByInsertedOrderByUpdateTimestampDesc(true);

        if (!latestUpdateTimestamp.isPresent()) {
            logger.info("No record was found on database, start getting transaction from {}", startDay);
            startDateTime = LocalDate.parse(startDay, DateTimeFormatter.ofPattern("yyyy-MMM-dd")).atStartOfDay();
            endDateTime = startDateTime.plusMinutes(2 * apiTimeIntervalInMinutes);
        } else {
            logger.info("Retrieved Latest UpdateTimestamp: {}", latestUpdateTimestamp.get().getUpdateTimestamp());
            LocalDateTime startDate = latestUpdateTimestamp.get().getUpdateTimestamp().toLocalDateTime();
            if (firstTimeRun == 1) {
                startDate = startDate.minusMinutes(startDayBackwardMin);
                firstTimeRun = 0;
            }
            startDateTime = startDate.minusMinutes(apiTimeIntervalInMinutes);
            endDateTime = startDateTime.plusMinutes(apiTimeIntervalInMinutes);
        }
        while (now.isAfter(startDateTime)) {
            Map<String, String> paramsMap = new HashMap<>();

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss z").withZone(ZoneId.of("UTC"));

            String startDateTimestamp = dateTimeFormatter.format(startDateTime);
            String endDateTimestamp = dateTimeFormatter.format(endDateTime);

            paramsMap.put("startUpdateTimestamp", startDateTimestamp);
            paramsMap.put("endUpdateTimestamp", endDateTimestamp);

            Map<String, String> reconQueryParameters = caspianConfiguration.caspianReconQueryParameters();
            reconQueryParameters.forEach(paramsMap::put);

            try {
                logger.info("Starting loading transaction which is updated at time from {} to {} from Caspian PMS, Parameters: {}", startDateTimestamp, endDateTimestamp, paramsMap);
                Optional<List<Transaction>> transactionList = Optional.ofNullable(transactionService.getTransactions(paramsMap, null));

                // handling logic for day which has no transaction
                transactionList.ifPresent(transactions -> {
                    logger.info("Got {} Transactions from Caspian PMS", transactions.size());

                    transactionsCounter.increment(transactions.size());

                    AtomicInteger noOfDataPushedToInflux = new AtomicInteger();
                    transactions.forEach(t -> {
                            TransactionMeasurement tm = dataFeedService.getTransactionDto(t, TransactionMeasurement.class);
                            changeTransactionMeasurementTradedAndSettleAndCurrencyCcy(tm);
                            TransactionEntity te = dataFeedService.getTransactionDto(t, TransactionEntity.class);
                            changeTransactionEntityTradedAndSettleAndCurrencyCcy(te);
                            String cashEntry = t.getCashEntry() == null ? "" : t.getCashEntry();
                            te.setCashEntry(cashEntry);
                            logger.trace("transaction: " + t.toString());
                            logger.trace("transaction measurement: " + tm.toString());
                            TransactionEntityPK tePK = new TransactionEntityPK(t.getId(), cashEntry);
                            // check whether the data is pushed or not
                            if (transactionRepository.existsById(tePK)) {
                                TransactionEntity targetTransaction = transactionRepository.findById(tePK).get();
                                LocalDateTime updateTimestampFromPms = te.getUpdateTimestamp().toLocalDateTime();
                                LocalDateTime updateTimestampFromDB = targetTransaction.getUpdateTimestamp().toLocalDateTime();

                                if (targetTransaction.getInserted() && updateTimestampFromPms.isEqual(updateTimestampFromDB)) {
                                    logger.trace("No update on this transaction");
                                    return;
                                }

                                if (isIgnoredForUpdateTransaction(t)) {
                                    logger.trace("Ignore update for transaction");
                                    transactionRepository.save(te);
                                    return;
                                }
                            }
                            // push data to influx and add to mysql
                            try {
                                dataFeedService.publishToInflux(tm);
                                te.setInserted(true);
                                transactionRepository.save(te);
                                noOfDataPushedToInflux.getAndIncrement();
                            } catch (Exception e) {
                                logger.info("Failed to push transaction into influx", e);
                                try {
                                    te.setInserted(false);
                                    transactionRepository.save(te);
                                } catch (Exception e2) {
                                    logger.error("Failed to update failed transaction to database", e2);
                                }
                            }
                    });
                    logger.info("Pushed {} transaction to influx from Caspian PMS ", noOfDataPushedToInflux.intValue());
                    pushedReconCounter.increment(noOfDataPushedToInflux.intValue());
                });

                startDateTime = endDateTime.minusMinutes(apiTimeIntervalInMinutes);
                endDateTime = endDateTime.plusMinutes(apiTimeIntervalInMinutes);

            } catch (HttpClientErrorException err) {
                logger.error("Failed to get transaction from Caspian PMS ", err);
                try {
                    logger.info("Caspian PMS requested failed with error: {}", getErrorDescription(err));
                } catch (Exception e) {
                    logger.error("Can't parse response with error ", e);
                }
                errorCounter.increment(1);
                throw err;
            } catch (Exception err) {
                logger.error("Error occurs ", err);
                errorCounter.increment(1);
                throw err;
            }
            // Sleep for 10s because of request limits of Caspian PMS REST API (10 times every minute)
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                logger.error("Error occurs when trying to sleep for 10s", e);
            }
        }
    }

    private boolean isIgnoredForUpdateTransaction(Transaction transaction) {
        return fundIgnoreList.contains(transaction.getFund()) && portfolioIgnoreList.contains(transaction.getPortfolio())
                && strategyIgnoreList.contains(transaction.getStrategy());
    }

    protected String getErrorDescription(HttpClientErrorException err) throws IOException {
        String response = err.getResponseBodyAsString();
        CaspianRestClientException exception = caspianConfiguration.objectMapper().readValue(response, CaspianRestClientException.class);
        return exception.getError();
    }

    protected void changeTransactionEntityTradedAndSettleAndCurrencyCcy(TransactionEntity transactionEntity){

        if (transactionEntity.getTradedCcy() == null || transactionEntity.getTradedCcy().trim().isEmpty() || transactionEntity.getTradedCcy().equals("null")){
            transactionEntity.setTradedCcy("null");
        }
        else {
            transactionEntity.setTradedCcy(transactionEntity.getTradedCcy());
        }
        if(transactionEntity.getTradedCcy() != null && transactionEntity.getTradedCcy().equals("BNP")) {
            transactionEntity.setTradedCcy("BAND");
        }
        if(transactionEntity.getTradedCcy() != null && transactionEntity.getTradedCcy().equals("DIN")) {
            transactionEntity.setTradedCcy("DAI");
        }
        if (transactionEntity.getSettleCcy() == null || transactionEntity.getSettleCcy().trim().isEmpty() || transactionEntity.getSettleCcy().equals("null")){
            transactionEntity.setSettleCcy("null");
        }
        else {
            transactionEntity.setSettleCcy(transactionEntity.getSettleCcy());
        }
        if(transactionEntity.getSettleCcy() != null && transactionEntity.getSettleCcy().equals("BNP")) {
            transactionEntity.setSettleCcy("BAND");
        }
        if(transactionEntity.getSettleCcy()!= null && transactionEntity.getSettleCcy().equals("DIN")) {
            transactionEntity.setSettleCcy("DAI");
        }

    }
    protected void changeTransactionMeasurementTradedAndSettleAndCurrencyCcy(TransactionMeasurement transactionMeasurement){
        if (transactionMeasurement.getTradedCcy() == null || transactionMeasurement.getTradedCcy().trim().isEmpty() || transactionMeasurement.getTradedCcy().equals("null")){
            transactionMeasurement.setTradedCcy("null");
        }
        else {
            transactionMeasurement.setTradedCcy(transactionMeasurement.getTradedCcy());
        }
        if(transactionMeasurement.getTradedCcy() != null && transactionMeasurement.getTradedCcy().equals("BNP")) {
            transactionMeasurement.setTradedCcy("BAND");
        }
        if(transactionMeasurement.getTradedCcy() != null && transactionMeasurement.getTradedCcy().equals("DIN")) {
            transactionMeasurement.setTradedCcy("DAI");
        }
        if (transactionMeasurement.getSettleCcy() == null || transactionMeasurement.getSettleCcy().trim().isEmpty() || transactionMeasurement.getSettleCcy().equals("null")){
            transactionMeasurement.setSettleCcy("null");
        }
        else {
            transactionMeasurement.setSettleCcy(transactionMeasurement.getSettleCcy());
        }
        if(transactionMeasurement.getSettleCcy() != null && transactionMeasurement.getSettleCcy().equals("BNP")) {
            transactionMeasurement.setSettleCcy("BAND");
        }
        if(transactionMeasurement.getSettleCcy()!= null && transactionMeasurement.getSettleCcy().equals("DIN")) {
            transactionMeasurement.setSettleCcy("DAI");
        }

    }
}
