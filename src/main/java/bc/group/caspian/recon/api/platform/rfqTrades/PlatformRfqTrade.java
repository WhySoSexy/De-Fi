
package bc.group.caspian.recon.api.platform.rfqTrades;

import bc.group.caspian.recon.api.platform.PlatformTradeSiteGroup;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformRfqTrade {

    private Boolean buyTradedCurrency;
    private String dateCreated;
    private Boolean enabledSimpleTradeWL;
    private Boolean enabledSimpleTradeWLSegWallet;
    private String forUserUuid;
    private String forUsername;
    private String lastUpdated;
    private String settlementCurrency;
    private String settlementCurrencyAmount;
    private String siteGroup;
    private String tradeId;
    private String tradeState;
    private String tradeUuid;
    private String tradedCurrency;
    private String tradedCurrencyAmount;
    private String treasuryUserSiteGroup;
    private String treasuryUserUuid;
    private String treasuryUsername;
    private String treasuryUserSettlementCurrency;
    private String treasuryUserSettlementAmount;

    public boolean containsSiteGroupForClientTradeMeasurement() {
        return (this.siteGroup.equals(PlatformTradeSiteGroup.OSLAM_GROUP.name())
                || this.siteGroup.equals(PlatformTradeSiteGroup.OSLLC_GROUP.name())
                || this.siteGroup.equals(PlatformTradeSiteGroup.OSLSG_GROUP.name())
                || this.siteGroup.equals(PlatformTradeSiteGroup.ZODIA_GROUP.name()));
    }

    public boolean containsSiteGroupForHedgeTradeMeasurement() {
        return (this.siteGroup.equals(PlatformTradeSiteGroup.OSLAM_GROUP.name())
                || this.siteGroup.equals(PlatformTradeSiteGroup.OSLLC_GROUP.name())
                || this.siteGroup.equals(PlatformTradeSiteGroup.ZODIA_GROUP.name()));
    }
}
