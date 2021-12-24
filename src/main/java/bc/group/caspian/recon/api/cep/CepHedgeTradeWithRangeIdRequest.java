package bc.group.caspian.recon.api.cep;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class CepHedgeTradeWithRangeIdRequest implements CepHedgeTradeRequest {

    private Long fromId;
    private Long toId;
}
