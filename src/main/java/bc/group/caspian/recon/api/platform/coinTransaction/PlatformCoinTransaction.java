package bc.group.caspian.recon.api.platform.coinTransaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformCoinTransaction {

    private String id;
    private String uuid;
    private String version;
    private String accountUuid;
    private String userUuid;
    private String siteGroup;
    private String ccy;
    private String amount;
    private String fee;
    private String networkFee;
    private String className;
    private String coinAddress;
    private int coinConfirmation;
    private String coinTransactionId;
    private String processedDateTime;
    private String receivedDateTime;
    private String transactionState;
    private String transactionType;

}
