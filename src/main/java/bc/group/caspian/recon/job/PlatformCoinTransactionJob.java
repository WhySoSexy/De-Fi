package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.platform.coinTransaction.PlatformCoinTransaction;
import bc.group.caspian.recon.api.platform.coinTransaction.PlatformCoinTransactionResponse;
import bc.group.caspian.recon.domain.mysql.SchedulerStatusEntity;
import bc.group.caspian.recon.domain.reconInflux.PlatformCoinTransactionMeasurement;
import bc.group.caspian.recon.repository.ScheduledStatusRepository;
import bc.group.caspian.recon.service.DataFeedService;
import bc.group.caspian.recon.service.PlatformCoinTransactionService;
import bc.group.caspian.recon.service.api.platform.PlatformEndpoint;
import bc.group.caspian.recon.service.config.PlatformCoinTransactionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlatformCoinTransactionJob {

    private final DataFeedService dataFeedService;
    private final PlatformCoinTransactionService platformCoinTransactionService;
    private final ScheduledStatusRepository scheduledStatusRepository;
    private final PlatformCoinTransactionProperties properties;
    private final static Logger logger = LoggerFactory.getLogger(PlatformCoinTransactionJob.class);

    public PlatformCoinTransactionJob(DataFeedService dataFeedService, PlatformCoinTransactionService platformCoinTransactionService, ScheduledStatusRepository scheduledStatusRepository, PlatformCoinTransactionProperties properties) {
        this.dataFeedService = dataFeedService;
        this.properties = properties;
        this.platformCoinTransactionService = platformCoinTransactionService;
        this.scheduledStatusRepository = scheduledStatusRepository;
    }

    public void runPlatformTransactionJob() {
        try {
            PlatformCoinTransactionResponse response = platformCoinTransactionService.getPlatformCoinTransactions();

            if (response.getResult() != null) {

                Long totalCount = response.getResult().getTotalCount();
                Long offset = response.getResult().getOffset();

                List<String> txnIds = new ArrayList<>();

                List<PlatformCoinTransaction> txns = response.getResult().getTransactions();

                for (PlatformCoinTransaction coinTransaction : txns) {
                    txnIds.add(String.valueOf(coinTransaction.getId()));
                }

                logger.info("Platform coin transaction response. totalCount: {}, offset: {}, txnIds: {}, txnSize: {}", totalCount, offset, txnIds, txns.size());

                if (!txns.isEmpty()) {
                    txns.forEach(this::process);

                    updateStatus(response);
                }
            } else {
                logger.info("Failed to process coin transaction, received transaction is null");
            }
        } catch (Exception e) {
            logger.error("Failed to execute platform coin transaction job", e);
        }
    }

    private void process(PlatformCoinTransaction txn) {
        try {
            saveToInflux(txn);
        } catch (Exception e) {
            logger.error("Failed to process platform coin transaction {} ", txn.getId(), e);
            throw e;
        }
    }

    private void saveToInflux(PlatformCoinTransaction txn) {
        try {
            PlatformCoinTransactionMeasurement measurement = PlatformCoinTransactionMeasurement.builder()
                    .amount(txn.getAmount())
                    .ccy(txn.getCcy())
                    .accountUuid(txn.getAccountUuid())
                    .className(txn.getClassName())
                    .coinAddress(txn.getCoinAddress())
                    .id(txn.getId())
                    .coinConfirmation(String.valueOf(txn.getCoinConfirmation()))
                    .coinTransactionId(txn.getCoinTransactionId())
                    .fee(txn.getFee())
                    .networkFee(txn.getNetworkFee())
                    .processedDateTime(txn.getProcessedDateTime())
                    .receivedDateTime(txn.getReceivedDateTime())
                    .siteGroup(txn.getSiteGroup())
                    .transactionState(txn.getTransactionState())
                    .transactionType(txn.getTransactionType())
                    .userUuid(txn.getUserUuid())
                    .uuid(txn.getUuid())
                    .version(txn.getVersion())
                    .build();
            dataFeedService.publishToInflux(measurement);
        } catch (Exception e) {
            logger.error("Failed to publish to influx platform coin transaction {} ", txn.getId(), e);
            throw e;
         }
    }

    private void updateStatus(PlatformCoinTransactionResponse response) {
        SchedulerStatusEntity status = scheduledStatusRepository.findSchedulerStatusEntityByName(PlatformEndpoint.COIN_TRANSACTION.name());
        status = status != null ? processStatus(status, response) : getStatus(response);
        scheduledStatusRepository.save(status);
    }

    private SchedulerStatusEntity processStatus(SchedulerStatusEntity status, PlatformCoinTransactionResponse response) {
        return isCompleted(status, response) ? setOffsetZeroAndUpdateTs(status, response) : updateOffset(status, response);
    }

    private SchedulerStatusEntity getStatus(PlatformCoinTransactionResponse response) {
        if (response.getResult().getTransactions().size() == response.getResult().getTotalCount()) {
            return SchedulerStatusEntity.builder()
                    .name(PlatformEndpoint.COIN_TRANSACTION.name())
                    .fromTs(getLastTimestamp(response))
                    .offset(0L)
                    .build();
        }

        return SchedulerStatusEntity.builder()
                .name(PlatformEndpoint.COIN_TRANSACTION.name())
                .fromTs(getDate(properties.getStartDate()))
                .offset(getOffset(response))
                .build();
    }

    private SchedulerStatusEntity setOffsetZeroAndUpdateTs(SchedulerStatusEntity status, PlatformCoinTransactionResponse response) {
        status.setOffset(0L);
        status.setFromTs(getLastTimestamp(response));
        return status;
    }

    private Timestamp getLastTimestamp(PlatformCoinTransactionResponse response) {
        List<PlatformCoinTransaction> txns = response.getResult().getTransactions();
        PlatformCoinTransaction lastTxn = txns.get(txns.size() - 1);
        LocalDateTime date = new Timestamp(new Long(lastTxn.getProcessedDateTime())).toLocalDateTime().plusNanos(1000000L);
        return Timestamp.valueOf(date);
    }

    private SchedulerStatusEntity updateOffset(SchedulerStatusEntity status, PlatformCoinTransactionResponse response) {
        status.setOffset(getOffsetAndListSizeSum(status, response));
        return status;
    }

    private boolean isCompleted(SchedulerStatusEntity status, PlatformCoinTransactionResponse response) {
        return getOffsetAndListSizeSum(status, response).equals(response.getResult().getTotalCount());
    }

    private Timestamp getDate(Long timestamp) {
        LocalDateTime date = new Timestamp(timestamp).toLocalDateTime();
        return Timestamp.valueOf(date);
    }

    private Long getOffset(PlatformCoinTransactionResponse response) {
        return (long) response.getResult().getTransactions().size();
    }

    private Long getOffsetAndListSizeSum(SchedulerStatusEntity status, PlatformCoinTransactionResponse response) {
        return Long.sum(status.getOffset(), response.getResult().getTransactions().size());
    }
}
