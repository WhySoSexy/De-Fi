package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.platform.otcBookingTrades.OtcTradeBooking;
import bc.group.caspian.recon.api.platform.otcBookingTrades.OtcTradeBookingResponse;
import bc.group.caspian.recon.api.platform.otcBookingTrades.OtcTradeBookingResult;
import bc.group.caspian.recon.domain.mysql.SchedulerStatusEntity;
import bc.group.caspian.recon.domain.reconInflux.OtcTradeMeasurement;
import bc.group.caspian.recon.domain.reconInflux.OtcVerifiedMeasurement;
import bc.group.caspian.recon.repository.OtcTradeBookingRepository;
import bc.group.caspian.recon.repository.ScheduledStatusRepository;
import bc.group.caspian.recon.service.DataFeedService;
import bc.group.caspian.recon.service.OtcTradeBookingService;
import bc.group.caspian.recon.service.config.PlatformOtcTradeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class OtcTradeBookingJobTest {
    @InjectMocks
    OtcTradeBookingJob scheduler;

    @Mock
    OtcTradeBookingRepository repository;

    @Mock
    ScheduledStatusRepository scheduledStatusRepository;

    @Mock
    PlatformOtcTradeProperties properties;

    @Mock
    DataFeedService dataFeedService;

    @Mock
    OtcTradeBookingService otcTradeBookingService;

    OtcTradeBooking trade;
    OtcTradeBookingResponse response;


    @BeforeEach
    public void init() {
        scheduler = new OtcTradeBookingJob(otcTradeBookingService, dataFeedService, repository, scheduledStatusRepository, properties);
        trade = createOtcTradeBooking(false, "1561712282000", "PLATFORM", "1", "OS-RFQ-30OCT19-3800-ETHUSD", "BTC", "OSL HK",
                "cc67ba0c-c229-4e1e-a537-a1790fce3ba6", "USD", new BigDecimal("-0.75"), new BigDecimal("100.000"), "11234");
        OtcTradeBookingResult res = new OtcTradeBookingResult();
        res.setCount(1L);
        res.setTrades(Collections.singletonList(trade));
        response = new OtcTradeBookingResponse();
        response.setResultCode("OK");
        response.setTimestamp("1612939977366");
        response.setResult(res);
    }

    @Test
    public void tesRunOtcTradeBookingsJobWithNullResponse() {
        Mockito.when(otcTradeBookingService.getOtcTradeBooking(any())).thenReturn(null);
        assertFalse(scheduler.runOtcTradeBookingsJob(OtcTradeBookingJob.VERIFIED));
    }

    @Test
    public void testRunOtcTradeBookingsJobWithLegitimateResponse() {
        Mockito.when(otcTradeBookingService.getOtcTradeBooking(any())).thenReturn(response);
        assertTrue(scheduler.runOtcTradeBookingsJob(OtcTradeBookingJob.VERIFIED));
    }

    @Test
    public void testRunOtcTradeBookingsJobWithException() {
        Mockito.doThrow(new RuntimeException()).when(otcTradeBookingService).getOtcTradeBooking(any());
        try {
            scheduler.runOtcTradeBookingsJob(OtcTradeBookingJob.VERIFIED);
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }
        assertFalse(scheduler.runOtcTradeBookingsJob(OtcTradeBookingJob.VERIFIED));
    }

    @Test
    public void testCorrectMeasurementIsCreatedAfterProcessingTrade() {
        String type = OtcTradeBookingJob.VERIFIED;
        OtcTradeMeasurement resultMeasurement = scheduler.createMeasurementForTrade(trade, type);
        assertTrue(resultMeasurement instanceof OtcVerifiedMeasurement);
    }

    @Test
    public void testStatusIsCompletedWhenSumOfOffsetAndTradesSizeIsEqualsToResponseResultCount() {
        Timestamp from = new Timestamp(1618891200000L);
        Timestamp to = new Timestamp(1619308800000L);
        SchedulerStatusEntity status = new SchedulerStatusEntity(1L, 0L, "test", from, to);

        assertEquals(response.getResult().getCount(), Long.sum(status.getOffset(), response.getResult().getTrades().size()));
        assertTrue(scheduler.isCompleted(response, status));
    }

    @Test
    public void testStatusIsNotCompletedWhenSumOfOffsetAndTradesSizeIsNotEqualsToResponseResultCount() {
        Timestamp from = new Timestamp(1618891200000L);
        Timestamp to = new Timestamp(1619308800000L);
        SchedulerStatusEntity status = new SchedulerStatusEntity(1L, 2L, "test", from, to);

        assertNotEquals(response.getResult().getCount(), Long.sum(status.getOffset(), response.getResult().getTrades().size()));
        assertFalse(scheduler.isCompleted(response, status));
    }

    @Test
    public void testForCompletedStatusSchedulerStatusEntityOffsetIsSetToZeroAndUpdatedTs(){
        Timestamp from = new Timestamp(1618891200000L);
        Timestamp to = new Timestamp(1619308800000L);
        SchedulerStatusEntity statusBefore = new SchedulerStatusEntity(1L, 0L, "test", from, to);
        assertTrue(scheduler.isCompleted(response, statusBefore));

        SchedulerStatusEntity statusAfter = scheduler.processStatus(statusBefore, response);

        assertEquals(0L, statusAfter.getOffset());
        assertEquals(toTimestamp(to.toLocalDateTime().minusSeconds(10L)), new Timestamp(statusAfter.getFromTs().getTime()));
        assertEquals(toTimestamp(to.toLocalDateTime().plusDays(5L)), new Timestamp(statusAfter.getToTs().getTime()));
    }

    @Test
    public void testForUncompletedStatusSchedulerStatusEntityOffsetIsSetOffsetAndTradesSizeSum(){
        Timestamp from = new Timestamp(1618891200000L);
        Timestamp to = new Timestamp(1619308800000L);
        SchedulerStatusEntity statusBefore = new SchedulerStatusEntity(1L, 3L, "test", from, to);
        Long offsetBefore = statusBefore.getOffset();
        assertFalse(scheduler.isCompleted(response, statusBefore));

        SchedulerStatusEntity statusAfter = scheduler.processStatus(statusBefore, response);
        Long offsetAfter = Long.sum(offsetBefore, response.getResult().getTrades().size());

        assertEquals(offsetAfter, statusAfter.getOffset());
        assertEquals(statusBefore.getFromTs(), statusAfter.getFromTs());
        assertEquals(statusBefore.getToTs(), statusAfter.getToTs());
    }

    @Test
    public void testCorrectSchedulerStatusEntityIsCreatedWhenTradesSizeIsEqualToRespondCount() {
        String type = OtcTradeBookingJob.VERIFIED;
        SchedulerStatusEntity entity = scheduler.createStatus(type, response);
        assertEquals(type, entity.getName());
        assertEquals(0L, entity.getOffset());
    }

    @Test
    public void testCorrectSchedulerStatusEntityIsCreatedWhenTradesSizeIsNotEqualToRespondCount() {
        OtcTradeBookingResult res = new OtcTradeBookingResult();
        res.setCount(2L);
        res.setTrades(Collections.singletonList(trade));
        response = new OtcTradeBookingResponse();
        response.setResultCode("OK");
        response.setTimestamp("1612939977366");
        response.setResult(res);
        String type = OtcTradeBookingJob.VERIFIED;
        SchedulerStatusEntity entity = scheduler.createStatus(type, response);
        assertEquals(type, entity.getName());
        assertEquals(response.getResult().getTrades().size(), entity.getOffset());
    }

    @Test
    public void testStatusShouldBeUpdatedIfItExists() {
        Timestamp from = new Timestamp(1618891200000L);
        Timestamp to = new Timestamp(1619308800000L);
        SchedulerStatusEntity entity = new SchedulerStatusEntity(1L, 3L, "test", from, to);
        Mockito.when(scheduledStatusRepository.findSchedulerStatusEntityByName(any())).thenReturn(entity);
        String type = OtcTradeBookingJob.VERIFIED;
        scheduler.updateStatus(response, type);
        assertEquals(Long.sum(3L, response.getResult().getTrades().size()), entity.getOffset());
    }

    private Timestamp toTimestamp(LocalDateTime date){
        return Timestamp.valueOf(date);
    }

    private OtcTradeBooking createOtcTradeBooking(Boolean buy, String tradeDate, String source, String tradeId, String tradeRef, String tradedCcy, String siteGroup,
                                                  String clientUuid, String settlementCcy, BigDecimal tradedQty, BigDecimal settlementQty, String approvalRequestId) {
        final OtcTradeBooking otcTradeBooking = new OtcTradeBooking();
        otcTradeBooking.setBuy(buy);
        otcTradeBooking.setTradeDate(tradeDate);
        otcTradeBooking.setSource(source);
        otcTradeBooking.setTradeId(tradeId);
        otcTradeBooking.setTradeRef(tradeRef);
        otcTradeBooking.setTradedCcy(tradedCcy);
        otcTradeBooking.setSiteGroup(siteGroup);
        otcTradeBooking.setClientUuid(clientUuid);
        otcTradeBooking.setSettlementCcy(settlementCcy);
        otcTradeBooking.setTradedQty(tradedQty);
        otcTradeBooking.setSettlementQty(settlementQty);
        otcTradeBooking.setApprovalRequestId(approvalRequestId);
        return otcTradeBooking;
    }
}
