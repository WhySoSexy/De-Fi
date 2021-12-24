
package bc.group.caspian.recon.api.platform.ledgerBalances;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LedgerBalancesResponse {

    private LedgerBalancesResult result;

    private String resultCode;

    private String timestamp;
}
