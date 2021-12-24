package bc.group.caspian.recon.api.platform.hedgeTrades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformHedgeTradeResult {

    private List<PlatformHedgeTrade> trades;
    private Long totalCount;
    private Long offset;
}
