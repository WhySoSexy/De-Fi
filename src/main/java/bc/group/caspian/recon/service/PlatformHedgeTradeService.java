package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.platform.hedgeTrades.*;

import bc.group.caspian.recon.domain.mysql.SchedulerStatusEntity;
import bc.group.caspian.recon.repository.ScheduledStatusRepository;
import bc.group.caspian.recon.service.api.platform.PlatformClient;
import bc.group.caspian.recon.service.api.platform.PlatformEndpoint;
import bc.group.caspian.recon.service.config.PlatformHedgeTradeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class PlatformHedgeTradeService {

    private final PlatformClient platformClient;
    private final ScheduledStatusRepository scheduledStatusRepository;
    private final static Logger logger = LoggerFactory.getLogger(PlatformHedgeTradeService.class);
    private final PlatformHedgeTradeProperties properties;

    public PlatformHedgeTradeService(PlatformClient platformClient, ScheduledStatusRepository scheduledStatusRepository, PlatformHedgeTradeProperties properties) {
        this.scheduledStatusRepository = scheduledStatusRepository;
        this.properties = properties;
        this.platformClient = platformClient;
    }

    public PlatformHedgeTradeResponse getPlatformHedgeTrades() {
        PlatformHedgeTradeResponse response;
        try {
            response = platformClient.getHedgeTrades(getHedgeTradeRequest());
        } catch (Exception e) {
            logger.error("Failed to get platform hedge trades : %s", e);
            throw e;
        }
        return response;
    }

    public PlatformHedgeTradeResponse getPlatformHedgeTradesRetry(List<Long> idList) {
        PlatformHedgeTradeResponse response;
        try {
            response = platformClient.getHedgeTrades(getHedgeTradeRetryRequest(idList));
        } catch (Exception e) {
            logger.error("Failed to get platform hedge trades retry : %s", e);
            throw e;
        }
        return response;
    }

    private PlatformHedgeTradeWithTimeRequest getHedgeTradeRequest() {
        SchedulerStatusEntity SchedulerStatus = scheduledStatusRepository.findSchedulerStatusEntityByName(PlatformEndpoint.HEDGE.name());
        return SchedulerStatus != null ? getRequestWithSchedulerStatus(SchedulerStatus) : getRequestWithSchedulerStatusNull();
    }

    private PlatformHedgeTradeWithIdListRequest getHedgeTradeRetryRequest(List<Long> idList) {
        return PlatformHedgeTradeWithIdListRequest.builder().tradeIdList(idList).build();
    }

    private PlatformHedgeTradeWithTimeRequest getRequestWithSchedulerStatus(SchedulerStatusEntity status) {
        return PlatformHedgeTradeWithTimeRequest.builder()
                .lastUpdatedFrom(status.getFromTs().toInstant().toString())
                .lastUpdatedTo(status.getToTs().toInstant().toString())
                .offset(status.getOffset())
                .batchSize(properties.getBatchSize())
                .siteGroup(properties.getSiteGroups())
                .build();
    }

    private PlatformHedgeTradeWithTimeRequest getRequestWithSchedulerStatusNull() {
        return PlatformHedgeTradeWithTimeRequest.builder()
                .lastUpdatedFrom(getDateMinusSeconds(properties.getStartDate()))
                .lastUpdatedTo(getDateWithBatch(properties.getStartDate()))
                .offset(0L)
                .batchSize(properties.getBatchSize())
                .siteGroup(properties.getSiteGroups())
                .build();
    }

    private String getDateWithBatch(Long timestamp) {
        return new Timestamp(timestamp).toLocalDateTime().plusDays(5L).toInstant(ZoneOffset.UTC).toString();
    }

    private String getDateMinusSeconds(Long timestamp) {
        return new Timestamp(timestamp).toLocalDateTime().minusSeconds(10L).toInstant(ZoneOffset.UTC).toString();
    }
}
