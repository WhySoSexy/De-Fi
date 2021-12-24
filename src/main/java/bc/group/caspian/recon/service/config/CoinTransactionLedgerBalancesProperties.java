package bc.group.caspian.recon.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "txn-ledger-balances")
@Component
@Data
public class CoinTransactionLedgerBalancesProperties {

    private Boolean jobEnabled;
}
