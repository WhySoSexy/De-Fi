package bc.group.caspian.recon.api.platform.coinTransactionLedgerBalances;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Data
public class CoinTransactionLedgerBalancesRequest {

    List<String> accountClasses;
}
