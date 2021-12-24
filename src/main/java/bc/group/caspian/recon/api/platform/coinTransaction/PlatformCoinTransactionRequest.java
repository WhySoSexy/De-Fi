package bc.group.caspian.recon.api.platform.coinTransaction;


import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Value
@Builder
@Data
public class PlatformCoinTransactionRequest {
    String fromTimestamp;
    Long batchSize;
    Long offset;
}
