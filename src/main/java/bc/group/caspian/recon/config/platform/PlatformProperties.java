package bc.group.caspian.recon.config.platform;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "platform")
@Component
@Data
public class PlatformProperties {

    private String host;
    private String health;
    private String otcEndpoint;
    private String hedgeEndpoint;
    private String ledgerBalancesEndpoint;
    private String rfqTradeEndpoint;
    private String coinTxnEndpoint;
    private String coinTxnLedgerBalancesEndpoint;
    private String apiKey;
    private String secret;
}
