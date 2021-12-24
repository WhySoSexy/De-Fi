package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.platform.coinTransactionLedgerBalances.CoinTransactionLedgerBalancesResponse;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;


@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CoinTransactionLedgerBalancesServiceTest {
    @Mock
    PlatformClient client;

    @InjectMocks
    CoinTransactionLedgerBalancesService service;

    @Value("${txn-ledger-account-classes}")
    public List<String> accountClasses;

    @Test
    public void getLedgerBalanceForTxns() {
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
        }};
        response.setResult(siteGroupMap);
        response.setResultCode("OK");
        response.setTimestamp("1612939977366");
        Mockito.when(client.getTxnLedgerBalances(any())).thenReturn(response);
        CoinTransactionLedgerBalancesResponse ledgerResponse = service.getLedgerBalances();
        assertEquals(ledgerResponse.getResult().size(), 1);
        assertEquals(ledgerResponse.getResult().get("OSLLC_GROUP"), accountClassMap);
        assertEquals(ledgerResponse.getResult().get("OSLLC_GROUP").get("TRADING"), coinMap);

    }
}
