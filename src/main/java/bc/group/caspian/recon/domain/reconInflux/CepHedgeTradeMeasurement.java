package bc.group.caspian.recon.domain.reconInflux;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;


@Measurement(name = "rfq_hedge_trade")
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CepHedgeTradeMeasurement {

    @Column(name = "trade_date")
    private String tradeDate;

    @Column(name = "venue")
    private String venue;

    @Column(name = "trade_ccy")
    private String tradeCcy;

    @Column(name = "trade_qty")
    private String tradeQty;

    @Column(name = "site_group")
    private String siteGroup;

    @Column(name = "settlement_ccy")
    private String settlementCcy;

    @Column(name = "settlement_qty")
    private String settlementQty;

    @Column(name = "hedge_id")
    private String hedgeId;

    @Column(name = "side")
    private String side;

    @Column(name = "cepId", tag = true)
    private String cepId;
}
