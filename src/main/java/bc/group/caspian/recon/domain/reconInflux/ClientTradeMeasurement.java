package bc.group.caspian.recon.domain.reconInflux;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name = "client_trade")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientTradeMeasurement {
    @Column(name = "trade_id")
    private String tradeId;

    @Column(name = "trade_uuid")
    private String tradeUuid;

    @Column(name = "date_created")
    private String dateCreated;

    @Column(name = "last_updated")
    private String lastUpdated;

    @Column(name = "buy_traded_currency")
    private String buyTradedCurrency;

    @Column(name = "traded_currency")
    private String tradedCurrency;

    @Column(name = "settlement_currency")
    private String settlementCurrency;

    @Column(name = "traded_currency_amount")
    private String tradedCurrencyAmount;

    @Column(name = "settlement_currency_amount")
    private String settlementCurrencyAmount;

    @Column(name = "for_user_uuid")
    private String forUserUuid;

    @Column(name = "for_username")
    private String forUsername;

    @Column(name = "site_group")
    private String siteGroup;

    @Column(name = "trade_state")
    private String tradeState;

    @Column(name = "treasury_user_uuid")
    private String treasuryUserUuid;

    @Column(name = "treasury_username")
    private String treasuryUsername;

    @Column(name = "treasury_user_site_group")
    private String treasuryUserSiteGroup;

    @Column(name = "treasury_user_settlement_currency")
    private String treasuryUserSettlementCurrency;

    @Column(name = "treasury_user_settlement_amount")
    private String treasuryUserSettlementAmount;

    @Column(name = "enabled_simple_trade_wl")
    private String enabledSimpleTradeWL;

    @Column(name = "enabled_simple_trade_wl_seg_wallet")
    private String enabledSimpleTradeWLSegWallet;
}
