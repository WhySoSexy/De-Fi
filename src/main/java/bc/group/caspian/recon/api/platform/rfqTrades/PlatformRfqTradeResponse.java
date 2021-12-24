
package bc.group.caspian.recon.api.platform.rfqTrades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformRfqTradeResponse {

    private PlatformRfqTradeResult result;
    private String resultCode;
    private String timestamp;
}
