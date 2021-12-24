package bc.group.caspian.recon.api.cep;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CepHedgeTradeWithIdListRequest implements CepHedgeTradeRequest {

    private List<Long> idList;
}
