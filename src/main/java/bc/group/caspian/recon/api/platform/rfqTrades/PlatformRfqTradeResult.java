
package bc.group.caspian.recon.api.platform.rfqTrades;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformRfqTradeResult {

    private Long offset;
    private Long totalCount;
    private List<PlatformRfqTrade> trades;
}
