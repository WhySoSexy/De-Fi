package bc.group.caspian.recon.domain.reconInflux;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name = "otc_rejected_post_verification_trade")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OtcRejectedPostVerificationMeasurement extends OtcTradeMeasurement {

    @Column(name = "trade_date")
    private String tradeDate;

    @Column(name = "source")
    private String source;

    @Column(name = "trade_id")
    private String tradeId;

    @Column(name = "trade_ref")
    private String tradeRef;

    @Column(name = "traded_ccy")
    private String tradedCcy;

    @Column(name = "site_group")
    private String siteGroup;

    @Column(name = "client_uuid")
    private String clientUuid;

    @Column(name = "trade_uuid")
    private String tradeUuid;

    @Column(name = "settlement_ccy")
    private String settlementCcy;

    @Column(name = "traded_qty")
    private String tradedQty;

    @Column(name = "side")
    private String side;

    @Column(name = "approval_request_id")
    private String approvalRequestId;

    @Column(name = "settlement_qty")
    private String settlementQty;
}
