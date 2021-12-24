package bc.group.caspian.recon.api.platform.otcBookingTrades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OtcTradeBookingResult {

    private Long count;
    private List<OtcTradeBooking> trades;
}
