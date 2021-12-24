package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.platform.otcBookingTrades.*;
import bc.group.caspian.recon.domain.mysql.SchedulerStatusEntity;
import bc.group.caspian.recon.job.OtcTradeBookingJob;
import bc.group.caspian.recon.repository.ScheduledStatusRepository;
import bc.group.caspian.recon.service.api.platform.PlatformClient;
import bc.group.caspian.recon.service.config.PlatformOtcTradeProperties;
import org.junit.jupiter.api.Disabled;
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
import java.time.ZoneOffset;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class OtcTradeBookingServiceTest {

    @Mock
    PlatformClient client;

    @Mock
    ScheduledStatusRepository repository;

    @InjectMocks
    OtcTradeBookingService otcTradeBookingService;

    @Mock
    PlatformOtcTradeProperties properties;

    @Test
    public void testGetOtcTradeBooking() {

        OtcTradeBooking trade = new OtcTradeBooking();
        trade.setClientUuid("cc67ba0c-c229-4e1e-a537-a1790fce3ba6");
        trade.setTradeRef("OS-RFQ-30OCT19-3800-ETHUSD");
        trade.setSettlementCcy("USD");
        trade.setTradedQty(new BigDecimal("-0.754195080000000000"));
        trade.setSettlementQty(new BigDecimal("100.000000000000000000"));
        trade.setTradedCcy("BTC");
        trade.setSiteGroup("OSL HK");
        trade.setSource("PLATFORM");
        trade.setTradeId("1");
        trade.setTradeDate("1561712282000");
        trade.setBuy(true);
        trade.setApprovalRequestId("11234");

        OtcTradeBookingResponse response = new OtcTradeBookingResponse();
        OtcTradeBookingResult res = new OtcTradeBookingResult();
        res.setCount(1L);
        res.setTrades(Collections.singletonList(trade));
        response.setResultCode("OK");
        response.setResult(res);

        Mockito.when(client.getOtcTrades(any())).thenReturn(response);
        Mockito.when(repository.findSchedulerStatusEntityByName(any())).thenReturn(null);
        Mockito.when(properties.getBatchSize()).thenReturn("20");
        Mockito.when(properties.getStartDate()).thenReturn(1601510400000L);
        Mockito.when(properties.getPreVerification()).thenReturn(Collections.singletonList("com.megaidea.domain.ops.OtcTradeBookingVerificationRequest"));
        Mockito.when(properties.getPostVerification()).thenReturn(Collections.singletonList("com.megaidea.domain.ops.OtcTradeBookingVerificationRequest"));
        OtcTradeBookingResponse result = otcTradeBookingService.getOtcTradeBooking(OtcTradeBookingJob.PENDING);
        OtcTradeBooking booking = result.getResult().getTrades().get(0);

        assertEquals(trade.getClientUuid(), booking.getClientUuid());
        assertEquals(trade.getSettlementCcy(), booking.getSettlementCcy());
        assertEquals(trade.getSettlementQty(), booking.getSettlementQty());
        assertEquals(trade.getSiteGroup(), booking.getSiteGroup());
        assertEquals(trade.getSource(), booking.getSource());
        assertEquals(trade.getTradeDate(), booking.getTradeDate());
        assertEquals(trade.getTradedCcy(), booking.getTradedCcy());
        assertEquals(trade.getTradedQty(), booking.getTradedQty());
        assertEquals(trade.getTradeId(), booking.getTradeId());
        assertEquals(trade.getTradeRef(), booking.getTradeRef());
        assertEquals(trade.getApprovalRequestId(), booking.getApprovalRequestId());
        assertEquals(trade.getBuy(), booking.getBuy());
    }

    @Test
    public void testOtcTradeBookingWithTimeRequestIsCorrectIfSchedulerStatusExists(){
        Timestamp from = new Timestamp(1618891200000L);
        Timestamp to = new Timestamp(1619308800000L);
        String type = OtcTradeBookingJob.PENDING;
        SchedulerStatusEntity status = new SchedulerStatusEntity(1L, 0L, type, from, to);
        OtcTradeBookingWithTimeRequest otcTradeBooking = otcTradeBookingService.getOtcTradeBookingRequest(type);

        assertEquals(status.getOffset(), otcTradeBooking.getOffset());
        assertEquals(type, otcTradeBooking.getApprovalStatus());
        assertEquals(properties.getPreVerification(), otcTradeBooking.getClassType());
    }

    @Test
    public void testOtcTradeBookingWithTimeRequestIsCorrectIfSchedulerStatusDoesNotExists(){
        String type = OtcTradeBookingJob.VERIFIED;
        OtcTradeBookingWithTimeRequest otcTradeBooking = otcTradeBookingService.getOtcTradeBookingRequest(type);

        assertEquals(0L, otcTradeBooking.getOffset());
        assertEquals(OtcStatusEnum.PROCESSED.name(), otcTradeBooking.getApprovalStatus());
        assertEquals(properties.getPreVerification(), otcTradeBooking.getClassType());
    }

    @Test
    public void testOtcTradeBookingWithTimeRequestIsCorrectForRequestWithSchedulerStatusNull(){
        String type = OtcTradeBookingJob.VERIFIED;
        OtcTradeBookingWithTimeRequest otcTradeBooking = otcTradeBookingService.getRequestWithSchedulerStatusNull(type);

        assertEquals(0L, otcTradeBooking.getOffset());
        assertEquals(OtcStatusEnum.PROCESSED.name(), otcTradeBooking.getApprovalStatus());
        assertEquals(properties.getPreVerification(), otcTradeBooking.getClassType());
        String expectedFrom = new Timestamp(properties.getStartDate()).toLocalDateTime().minusSeconds(10L).toInstant(ZoneOffset.UTC).toString();
        assertEquals(expectedFrom, otcTradeBooking.getFrom());
        String expectedTo = new Timestamp(properties.getStartDate()).toLocalDateTime().plusDays(5L).toInstant(ZoneOffset.UTC).toString();
        assertEquals(expectedTo, otcTradeBooking.getTo());
        assertEquals(properties.getBatchSize(), otcTradeBooking.getBatchSize());
    }

    @Test
    public void testOtcTradeBookingWithTimeRequestIsCorrectForRequestWithSchedulerStatus(){
        Timestamp from = new Timestamp(1618891200000L);
        Timestamp to = new Timestamp(1619308800000L);
        String type = OtcTradeBookingJob.PENDING;
        SchedulerStatusEntity status = new SchedulerStatusEntity(1L, 0L, type, from, to);
        OtcTradeBookingWithTimeRequest otcTradeBooking = otcTradeBookingService.getOtcTradeBookingRequest(type);

        assertEquals(status.getOffset(), otcTradeBooking.getOffset());
        assertEquals(type, otcTradeBooking.getApprovalStatus());
        assertEquals(properties.getPreVerification(), otcTradeBooking.getClassType());
        assertEquals(properties.getBatchSize(), otcTradeBooking.getBatchSize());
    }
}
