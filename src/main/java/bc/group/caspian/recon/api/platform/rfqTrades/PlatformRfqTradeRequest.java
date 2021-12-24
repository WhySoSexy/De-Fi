package bc.group.caspian.recon.api.platform.rfqTrades;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlatformRfqTradeRequest {

    private String dateCreatedFrom;
    private String dateCreatedTo;
    private List<String> siteGroups;
    private List<String> tradeIdList;
    private Long batchSize;
    private Long offset;
}
