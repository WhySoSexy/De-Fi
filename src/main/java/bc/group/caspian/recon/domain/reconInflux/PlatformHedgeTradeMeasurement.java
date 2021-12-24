package bc.group.caspian.recon.domain.reconInflux;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name = "platform_hedge_trade")
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformHedgeTradeMeasurement {

    @Column(name = "source")
    private String source;

    @Column(name = "trade_id")
    private String tradeId;

    @Column(name = "transaction_uuid")
    private String transactionUuid;

    @Column(name = "user_uuid")
    private String userUuid;

    @Column(name = "site_group")
    private String siteGroup;

    @Column(name = "buy_traded_currency")
    private String buyTradedCurrency;

    @Column(name = "ccy")
    private String ccy;

    @Column(name = "amount")
    private String amount;

    @Column(name = "side")
    private String side;

    @Column(name = "processed_date")
    private String processedDate;
}
