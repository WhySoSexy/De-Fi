package bc.group.caspian.recon.api.platform.hedgeTrades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformHedgeTradeResponse {

    private PlatformHedgeTradeResult result;
    private String timestamp;
    private String resultCode;
}
