package bc.group.caspian.recon.api.platform.coinTransaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformCoinTransactionResponse {

    private PlatformCoinTransactionResult result;
    private String timestamp;
    private String resultCode;
}
