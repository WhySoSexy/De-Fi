package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.platform.otcBookingTrades.OtcTradeBookingResponse;
import bc.group.caspian.recon.api.platform.TradeSideEnum;
import bc.group.caspian.recon.domain.mysql.OtcTradeBookingEntity;
import bc.group.caspian.recon.domain.mysql.SchedulerStatusEntity;
import bc.group.caspian.recon.repository.OtcTradeBookingRepository;
import bc.group.caspian.recon.repository.ScheduledStatusRepository;
import bc.group.caspian.recon.service.DataFeedService;
import bc.group.caspian.recon.service.OtcTradeBookingService;
import bc.group.caspian.recon.domain.reconInflux.*;
import bc.group.caspian.recon.api.platform.otcBookingTrades.OtcTradeBooking;

import bc.group.caspian.recon.service.config.PlatformOtcTradeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OtcTradeBookingJob {

    public static final String PENDING = "PENDING";
    public static final String VERIFIED = "VERIFIED";
    public static final String PRE_VERIFICATION = "PRE_VERIFICATION";
    public static final String POST_VERIFICATION = "POST_VERIFICATION";

    private final DataFeedService dataFeedService;
    private final PlatformOtcTradeProperties properties;
    private final OtcTradeBookingService otcTradeBookingService;
    private final ScheduledStatusRepository scheduledStatusRepository;
    private final OtcTradeBookingRepository repository;
    private final static Logger logger = LoggerFactory.getLogger(OtcTradeBookingJob.class);

    public OtcTradeBookingJob(OtcTradeBookingService otcTradeBookingService, DataFeedService dataFeedService, OtcTradeBookingRepository repository, ScheduledStatusRepository scheduledStatusRepository, PlatformOtcTradeProperties properties) {
        this.dataFeedService = dataFeedService;
        this.properties = properties;
        this.otcTradeBookingService = otcTradeBookingService;
        this.repository = repository;
        this.scheduledStatusRepository = scheduledStatusRepository;
    }

    public boolean runOtcTradeBookingsJob(String otcBookingType) {
        try {
            OtcTradeBookingResponse response = otcTradeBookingService.getOtcTradeBooking(otcBookingType);

            if (response != null && response.getResult() != null) {
                List<OtcTradeBooking> trades = response.getResult().getTrades();
                logger.info("Got {} new otc trade bookings from Platform api", trades.size());

                trades.forEach(t -> process(t, otcBookingType));
                updateStatus(response, otcBookingType);
                return true;
            } else {
                logger.info("Failed to process otc trades, value is null");
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to execute otc bookings job", e);
            return false;
        }
    }

    protected void process(OtcTradeBooking trade, String otcBookingType) {
        try {
            if (repository.findOtcTradeBookingEntityByTradeIdAndOtcBookingType(Long.parseLong(trade.getTradeId()), otcBookingType) == null ) {
                dataFeedService.publishToInflux(createMeasurementForTrade(trade, otcBookingType));

                OtcTradeBookingEntity entity = OtcTradeBookingEntity.builder()
                        .tradeId(Long.parseLong(trade.getTradeId()))
                        .otcBookingType(otcBookingType)
                        .lastUpdated(new Timestamp(Long.parseLong(trade.getTradeDate())))
                        .build();
                repository.save(entity);
            }
        } catch (Exception e) {
            logger.error("Failed to publish to influx {}", otcBookingType, e);
            throw e;
        }
    }

    protected OtcTradeMeasurement createMeasurementForTrade(OtcTradeBooking trade, String otcBookingType) {
        OtcTradeMeasurement measurement = selectMeasurementType(otcBookingType);
        measurement.setSiteGroup(trade.getSiteGroup());
        measurement.setTradeDate(trade.getTradeDate());
        measurement.setTradedCcy(trade.getTradedCcy());
        measurement.setSource(trade.getSource());
        measurement.setTradeId(trade.getTradeId());
        measurement.setTradeRef(trade.getTradeRef());
        measurement.setClientUuid(trade.getClientUuid());
        measurement.setTradeUuid(trade.getTradeUuid());
        measurement.setSettlementCcy(trade.getSettlementCcy());
        measurement.setApprovalRequestId(trade.getApprovalRequestId());
        measurement.setSide(trade.getBuy() ? TradeSideEnum.BUY.name() : TradeSideEnum.SELL.name());
        measurement.setTradedQty(String.valueOf(trade.getTradedQty()));
        measurement.setSettlementQty(String.valueOf(trade.getSettlementQty()));
        return measurement;
    }

    private OtcTradeMeasurement selectMeasurementType(String otcBookingType) {
        switch (otcBookingType) {
            case PENDING:
                return new OtcPendingMeasurement();
            case VERIFIED:
                return new OtcVerifiedMeasurement();
            case PRE_VERIFICATION:
                return new OtcRejectedPreVerificationMeasurement();
            default:
                return new OtcRejectedPostVerificationMeasurement();
        }
    }

    protected void updateStatus(OtcTradeBookingResponse response, String otcBookingType) {
        SchedulerStatusEntity status = scheduledStatusRepository.findSchedulerStatusEntityByName(otcBookingType);
        status = status != null ? processStatus(status, response) : createStatus(otcBookingType, response);
        scheduledStatusRepository.save(status);
    }

    protected SchedulerStatusEntity processStatus(SchedulerStatusEntity status, OtcTradeBookingResponse response) {
        return isCompleted(response, status) ? setOffsetZeroAndUpdateTs(status) : updateOffset(status, response);
    }

    protected SchedulerStatusEntity createStatus(String otcBookingType, OtcTradeBookingResponse response) {
        if (response.getResult().getTrades().size() == response.getResult().getCount()) {
            return SchedulerStatusEntity.builder()
                    .name(otcBookingType)
                    .fromTs(getDateMinusSeconds(getDateWithBatch(properties.getStartDate()).getTime()))
                    .toTs(getDateWithBatch(getDateWithBatch(properties.getStartDate()).getTime()))
                    .offset(0L)
                    .build();
        }

        return SchedulerStatusEntity.builder()
                .name(otcBookingType)
                .fromTs(getDateMinusSeconds(properties.getStartDate()))
                .toTs(getDateWithBatch(properties.getStartDate()))
                .offset(getOffset(response))
                .build();
    }

    private SchedulerStatusEntity setOffsetZeroAndUpdateTs(SchedulerStatusEntity status) {
        status.setOffset(0L);
        status.setFromTs(getDateMinusSeconds(status.getToTs().getTime()));
        status.setToTs(getDateWithBatch(status.getToTs().getTime()));
        return status;
    }

    private SchedulerStatusEntity updateOffset(SchedulerStatusEntity status, OtcTradeBookingResponse response) {
        status.setOffset(countOffsetAndTradesSizeSum(status, response));
        return status;
    }

    protected boolean isCompleted(OtcTradeBookingResponse response, SchedulerStatusEntity status) {
        return countOffsetAndTradesSizeSum(status, response).equals(response.getResult().getCount());
    }

    private Timestamp getDateWithBatch(Long timestamp) {
        LocalDateTime date = new Timestamp(timestamp).toLocalDateTime().plusDays(5L);
        if (date.isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
            return Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return Timestamp.valueOf(date);
    }

    private Timestamp getDateMinusSeconds(Long timestamp) {
        LocalDateTime date = new Timestamp(timestamp).toLocalDateTime().minusSeconds(10L);
        return Timestamp.valueOf(date);
    }

    private Long getOffset(OtcTradeBookingResponse response) {
        return (long) response.getResult().getTrades().size();
    }

    private Long countOffsetAndTradesSizeSum(SchedulerStatusEntity status, OtcTradeBookingResponse response) {
        return Long.sum(status.getOffset(), response.getResult().getTrades().size());
    }
}
