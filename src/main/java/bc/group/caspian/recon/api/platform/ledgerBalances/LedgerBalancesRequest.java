package bc.group.caspian.recon.api.platform.ledgerBalances;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
@Builder
@Data
public class LedgerBalancesRequest {

    List<String> userUuids;

    List<String> currencies;
}
