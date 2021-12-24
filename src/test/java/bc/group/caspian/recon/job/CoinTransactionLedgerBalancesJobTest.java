package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.platform.coinTransactionLedgerBalances.CoinTransactionLedgerBalancesResponse;
import bc.group.caspian.recon.api.platform.coinTransactionLedgerBalances.LedgerBalances;
import bc.group.caspian.recon.service.CoinTransactionLedgerBalancesService;
import bc.group.caspian.recon.service.api.platform.PlatformClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class CoinTransactionLedgerBalancesJobTest {

    @InjectMocks
    CoinTransactionLedgerBalancesJob coinTransactionLedgerBalancesJob;

    @Value("${txn-ledger-ds-site-group}")
    private List<String> DsSiteGroups;

    @Value("${txn-ledger-site-groups-disabled}")
    private List<String> DisabledSiteGroup;

    @Mock
    CoinTransactionLedgerBalancesService service;

    @Mock
    PlatformClient client;

    @Test
    public void LedgerBalanceJob() {
        CoinTransactionLedgerBalancesResponse response = getApiResponse();

        Mockito.when(client.getTxnLedgerBalances(any())).thenReturn(response);
        Mockito.when(service.getLedgerBalances()).thenReturn(response);
        coinTransactionLedgerBalancesJob.runTxnGetLedgerBalanceJob();
        Map<String, LedgerBalances> oldBalances =  getOldApiResponse();
        // old should contains currency from api response
        assertEquals(true, oldBalances.containsKey("BTC"));
        assertEquals(true, oldBalances.containsKey("USDT"));

        //when its ds site group balance should be save in ds_balance
        assertEquals("10000", oldBalances.get("BTC").getDs().toPlainString());
        assertEquals("0", oldBalances.get("BTC").getNonDs().toPlainString());

        assertEquals("100", oldBalances.get("USDT").getDs().toPlainString());
        assertEquals("0", oldBalances.get("USDT").getNonDs().toPlainString());

    }

    private CoinTransactionLedgerBalancesResponse getApiResponse(){
        CoinTransactionLedgerBalancesResponse response = new CoinTransactionLedgerBalancesResponse();
        Map<String, String> coinMap = new HashMap<String, String>() {{
            put("BTC", "10000");
            put("USDT", "100");
        }};
        Map<String, Map<String, String>> accountClassMap = new HashMap<String, Map<String, String>>() {{
            put("TRADING", coinMap);
        }};
        Map<String, Map<String, Map<String, String>>> siteGroupMap = new HashMap<String, Map<String, Map<String, String>>>() {{
            put("OSLLC_GROUP", accountClassMap);
            put("DBS_GROUP", accountClassMap);
        }};
        response.setResult(siteGroupMap);
        response.setResultCode("OK");
        response.setTimestamp("1612939977366");
        return response;
    }

    private Map<String, LedgerBalances> getOldApiResponse(){
        Map<String, LedgerBalances> coinMap = new HashMap<String, LedgerBalances>() {{
            put("BTC", new LedgerBalances(BigDecimal.ZERO, new BigDecimal("10000")));
            put("USDT", new LedgerBalances(BigDecimal.ZERO, new BigDecimal("100")));
        }};
        return coinMap;
    }


}
