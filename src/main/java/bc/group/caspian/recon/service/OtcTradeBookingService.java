package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.platform.otcBookingTrades.OtcStatusEnum;
import bc.group.caspian.recon.api.platform.otcBookingTrades.OtcTradeBookingResponse;
import bc.group.caspian.recon.api.platform.otcBookingTrades.OtcTradeBookingWithTimeRequest;
import bc.group.caspian.recon.domain.mysql.SchedulerStatusEntity;
import bc.group.caspian.recon.repository.ScheduledStatusRepository;
import bc.group.caspian.recon.job.OtcTradeBookingJob;
import bc.group.caspian.recon.service.api.platform.PlatformClient;
import bc.group.caspian.recon.service.config.PlatformOtcTradeProperties;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Slf4j
public class OtcTradeBookingService {

    private final PlatformClient platformClient;
    private final ScheduledStatusRepository scheduledStatusRepository;
    private final PlatformOtcTradeProperties properties;
    private final static Logger logger = LoggerFactory.getLogger(OtcTradeBookingService.class);

    public OtcTradeBookingService(PlatformClient platformClient, ScheduledStatusRepository scheduledStatusRepository, PlatformOtcTradeProperties properties) {
        this.scheduledStatusRepository = scheduledStatusRepository;
        this.properties = properties;
        this.platformClient = platformClient;
    }

    public OtcTradeBookingResponse getOtcTradeBooking(String otcBookingType) {
        OtcTradeBookingResponse response;
        try {
            response = platformClient.getOtcTrades(getOtcTradeBookingRequest(otcBookingType));
        } catch (Exception e) {
            logger.error("Failed to get otc trade bookings: %s", e);
            throw e;
        }
        return response;
    }

    protected OtcTradeBookingWithTimeRequest getOtcTradeBookingRequest(String otcBookingType) {
        SchedulerStatusEntity schedulerStatus = scheduledStatusRepository.findSchedulerStatusEntityByName(otcBookingType);
        return schedulerStatus != null ? getRequestWithSchedulerStatus(schedulerStatus, otcBookingType) : getRequestWithSchedulerStatusNull(otcBookingType);
    }

    private OtcTradeBookingWithTimeRequest getRequestWithSchedulerStatus(SchedulerStatusEntity status, String otcBookingType) {
        return OtcTradeBookingWithTimeRequest.builder()
                .from(status.getFromTs().toInstant().toString())
                .to(status.getToTs().toInstant().toString())
                .offset(status.getOffset())
                .batchSize(properties.getBatchSize())
                .classType(getClassTypes(otcBookingType))
                .approvalStatus(getStatus(otcBookingType))
                .build();
    }

    protected OtcTradeBookingWithTimeRequest getRequestWithSchedulerStatusNull(String otcBookingType) {
        return OtcTradeBookingWithTimeRequest.builder()
                .from(getDateMinusSeconds(properties.getStartDate()))
                .to(getDateWithBatch(properties.getStartDate()))
                .batchSize(properties.getBatchSize())
                .offset(0L)
                .classType((getClassTypes(otcBookingType)))
                .approvalStatus(getStatus(otcBookingType))
                .build();
    }

    private List<String> getClassTypes(String otcBookingType) {
        return otcBookingType.equals(OtcTradeBookingJob.POST_VERIFICATION) ? properties.getPostVerification() : properties.getPreVerification();
    }

    private String getStatus(String otcBookingType) {
        if (otcBookingType.equals(OtcTradeBookingJob.PENDING)) return OtcStatusEnum.PENDING.name();
        if (otcBookingType.equals(OtcTradeBookingJob.VERIFIED)) return OtcStatusEnum.PROCESSED.name();
        return OtcStatusEnum.REJECTED.name();
    }

    private String getDateWithBatch(Long timestamp) {
        return new Timestamp(timestamp).toLocalDateTime().plusDays(5L).toInstant(ZoneOffset.UTC).toString();
    }

    private String getDateMinusSeconds(Long timestamp) {
        return new Timestamp(timestamp).toLocalDateTime().minusSeconds(10L).toInstant(ZoneOffset.UTC).toString();
    }
}
