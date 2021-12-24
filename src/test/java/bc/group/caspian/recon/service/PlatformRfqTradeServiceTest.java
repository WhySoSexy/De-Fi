package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.platform.rfqTrades.PlatformRfqTrade;
import bc.group.caspian.recon.api.platform.rfqTrades.PlatformRfqTradeResponse;
import bc.group.caspian.recon.api.platform.rfqTrades.PlatformRfqTradeResult;
import bc.group.caspian.recon.service.api.platform.PlatformClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class PlatformRfqTradeServiceTest {

    @Mock
    PlatformClient client;

    @InjectMocks
    PlatformRfqTradeService service;

    PlatformRfqTradeResponse response;

    @BeforeEach
    public void init() {
        PlatformRfqTrade trade = new PlatformRfqTrade();
        trade.setTradeId("rfq-trade-0");
        trade.setTradeUuid("62ed1d35-cfcc-4e3b-95f9-1a41437ed17f");
        trade.setDateCreated("2021-04-23T15:07:19.000+08:00");
        trade.setLastUpdated("2021-04-23T16:17:45.000+08:00");
        trade.setBuyTradedCurrency(true);
        trade.setTradedCurrency("BTC");
        trade.setSettlementCurrency("USD");
        trade.setTradedCurrencyAmount("0.00906430");
        trade.setSettlementCurrencyAmount("450.00000000");
        trade.setForUsername("christine.sze+client@osl.com");
        trade.setForUserUuid("828093eb-cf24-42f2-85b3-b2fe3d84d476");
        trade.setSiteGroup("OSLLC_GROUP");
        trade.setTradeState("DVP_PTS_SETTLED");
        trade.setTreasuryUserUuid("7afe7ebc-d096-4be7-9580-2c36f118eb49");
        trade.setTreasuryUsername("prakash.konagi+treasury_ds@osl.com");
        trade.setTreasuryUserSiteGroup("OSLSG_GROUP");
        trade.setEnabledSimpleTradeWL(false);
        trade.setEnabledSimpleTradeWLSegWallet(true);

        response = new PlatformRfqTradeResponse();
        PlatformRfqTradeResult result = new PlatformRfqTradeResult();
        result.setTrades(Collections.singletonList(trade));
        result.setOffset(0L);
        result.setTotalCount(1L);

        response.setResult(result);
    }

    @Test
    public void testGetRfqTradeResultWithTimeframe() {
        Mockito.when(client.getRfqTrade(any())).thenReturn(response);
        String from = "2021-04-05T01:14:00+0800";
        String to = "2021-04-06T01:14:00+0800";
        Optional<PlatformRfqTradeResult> optionalPlatformRfqTradeResult = service
                .getRfqTradesResult(from, to, Arrays.asList("OSLLC_GROUP"), 30L, 0L);
        assertTrue(optionalPlatformRfqTradeResult.isPresent());
        PlatformRfqTradeResult result = optionalPlatformRfqTradeResult.get();
        assertEquals(response.getResult().getOffset(), result.getOffset());
        assertEquals(response.getResult().getTotalCount(), result.getTotalCount());
        assertEquals(response.getResult().getTrades().size(), result.getTrades().size());
        assertEquals(response.getResult().getTrades().get(0).getTradeId(),
                result.getTrades().get(0).getTradeId());
    }

    @Test
    public void testGetRfqTradeResultWithTradeIds() {
        Mockito.when(client.getRfqTrade(any())).thenReturn(response);
        Optional<PlatformRfqTradeResult> optionalPlatformRfqTradeResult = service
                .getRfqTradesResult(Arrays.asList("trade_id"), 30L, 0L);
        assertTrue(optionalPlatformRfqTradeResult.isPresent());
        PlatformRfqTradeResult result = optionalPlatformRfqTradeResult.get();
        assertEquals(response.getResult().getOffset(), result.getOffset());
        assertEquals(response.getResult().getTotalCount(), result.getTotalCount());
        assertEquals(response.getResult().getTrades().size(), result.getTrades().size());
        assertEquals(response.getResult().getTrades().get(0).getTradeId(),
                result.getTrades().get(0).getTradeId());
    }
}