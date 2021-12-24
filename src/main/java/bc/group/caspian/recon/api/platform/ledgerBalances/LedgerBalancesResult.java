
package bc.group.caspian.recon.api.platform.ledgerBalances;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LedgerBalancesResult {

    private Long count;

    private List<LedgerBalancesUser> users;
}
