package bc.group.caspian.recon.api.platform.coinTransaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformCoinTransactionResult {

    private List<PlatformCoinTransaction> transactions;
    private Long totalCount;
    private Long offset;
}
