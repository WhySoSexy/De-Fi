package bc.group.caspian.recon.domain.reconInflux.accountbalance;

import lombok.Data;

@Data
public class AccountBalanceMeasurement {
    private String account;
    private String site;
    private String ccy;
    private String balance;
}
