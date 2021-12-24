package bc.group.caspian.recon.api.platform.hedgeTrades;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Data
public class PlatformHedgeTradeWithTimeRequest implements PlatformHedgeTradeRequest{

    String batchSize;
    String lastUpdatedFrom;
    String lastUpdatedTo;
    List<String> siteGroup;
    Long offset;
}
