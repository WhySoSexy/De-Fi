package bc.group.caspian.recon.api.cep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CepHedgeTrade {

    private String id;
    private String fillId;
    private String ccyPair;
    private String customer;
    private String quoteProvider;
    private BigDecimal settlementCurrencyAmount;
    private String settlementCurrency;
    private BigDecimal tradedCurrencyAmount;
    private String originTradeId;
    private String tradedCurrency;
    private String treasuryUserSiteGroup;
    private String treasuryUser;
    private Boolean userBuy;
    private Boolean hedgeBuy;
    private String venue;
    private String status;
    private String siteGroup;
    private BigDecimal price;
    private BigDecimal dateCreated;
    private BigDecimal lastUpdated;
}
