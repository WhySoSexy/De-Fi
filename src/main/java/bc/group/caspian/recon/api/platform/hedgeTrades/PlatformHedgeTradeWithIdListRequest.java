package bc.group.caspian.recon.api.platform.hedgeTrades;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Data
public class PlatformHedgeTradeWithIdListRequest implements PlatformHedgeTradeRequest{

    List<Long> tradeIdList;
}
