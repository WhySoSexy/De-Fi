package bc.group.caspian.recon.api.platform.coinTransactionLedgerBalances;

import lombok.Data;

import java.util.Map;

@Data
public class CoinTransactionLedgerBalancesResponse {
    private Map<String, Map<String, Map<String, String>>> result;
    private String timestamp;
    private String resultCode;

}
