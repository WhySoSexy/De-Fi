package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.platform.coinTransaction.PlatformCoinTransactionRequest;
import bc.group.caspian.recon.api.platform.coinTransaction.PlatformCoinTransactionResponse;
import bc.group.caspian.recon.domain.mysql.SchedulerStatusEntity;
import bc.group.caspian.recon.repository.ScheduledStatusRepository;
import bc.group.caspian.recon.service.api.platform.PlatformClient;
import bc.group.caspian.recon.service.api.platform.PlatformEndpoint;
import bc.group.caspian.recon.service.config.PlatformCoinTransactionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PlatformCoinTransactionService {

    private final PlatformClient platformClient;
    private final ScheduledStatusRepository scheduledStatusRepository;
    private final static Logger logger = LoggerFactory.getLogger(PlatformCoinTransactionService.class);
    private final PlatformCoinTransactionProperties properties;

    public PlatformCoinTransactionService(PlatformClient platformClient, ScheduledStatusRepository scheduledStatusRepository, PlatformCoinTransactionProperties properties) {
        this.scheduledStatusRepository = scheduledStatusRepository;
        this.properties = properties;
        this.platformClient = platformClient;
    }

    public PlatformCoinTransactionResponse getPlatformCoinTransactions() {
        PlatformCoinTransactionResponse response;
        try {
            response = platformClient.getCoinTransactions(getCoinTransactionRequest());
        } catch (Exception e) {
            logger.error("Failed to get platform transaction : %s", e);
            throw e;
        }
        return response;
    }

    private PlatformCoinTransactionRequest getCoinTransactionRequest() {
        SchedulerStatusEntity SchedulerStatus = scheduledStatusRepository.findSchedulerStatusEntityByName(PlatformEndpoint.COIN_TRANSACTION.name());
        return SchedulerStatus != null ? getRequestWithSchedulerStatus(SchedulerStatus) : getRequestWithSchedulerStatusNull();
    }

    private  PlatformCoinTransactionRequest getRequestWithSchedulerStatus(SchedulerStatusEntity status) {
        return PlatformCoinTransactionRequest.builder()
                .fromTimestamp(String.valueOf(status.getFromTs().toInstant().toEpochMilli()))
                .offset(status.getOffset())
                .batchSize(properties.getBatchSize())
                .build();
    }
    private PlatformCoinTransactionRequest getRequestWithSchedulerStatusNull() {
        return PlatformCoinTransactionRequest.builder()
                .fromTimestamp(properties.getStartDate().toString())
                .offset(0L)
                .batchSize(properties.getBatchSize())
                .build();
    }
}
