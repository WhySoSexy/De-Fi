package bc.group.caspian.recon.api.platform.ledgerBalances;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LedgerBalancesUser {
    private String userUuid;

    private String username;

    private String siteGroup;

    private List<LedgerBalancesAccountBalance> accountBalances;
}
