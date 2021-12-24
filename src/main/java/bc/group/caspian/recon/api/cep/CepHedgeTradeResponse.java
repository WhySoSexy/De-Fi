package bc.group.caspian.recon.api.cep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CepHedgeTradeResponse {

    private List<CepHedgeTrade> rfqTradeList;
    private List<CepHedgeTrade> rfqTradeFillList;
    private String latestId;
}
