package bc.group.caspian.recon.api.platform.hedgeTrades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformHedgeFloatTransaction {

    private String transactionId;
    private String userUuid;
    private String username;
    private String siteGroup;
    private String ccy;
    private String state;
    private String txnType;
    private String receivedDateTime;
    private String processedDateTime;
    private String originatingTransactionId;
    private BigDecimal amount;
}
