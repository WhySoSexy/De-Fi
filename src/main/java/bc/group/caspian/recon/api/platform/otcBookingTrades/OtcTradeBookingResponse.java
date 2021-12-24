package bc.group.caspian.recon.api.platform.otcBookingTrades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OtcTradeBookingResponse {

    private OtcTradeBookingResult result;
    private String resultCode;
    private String timestamp;
}
