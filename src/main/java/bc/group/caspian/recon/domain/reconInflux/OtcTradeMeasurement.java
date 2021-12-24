package bc.group.caspian.recon.domain.reconInflux;

import lombok.Data;

@Data
public abstract class OtcTradeMeasurement {

    private String tradeDate;
    private String source;
    private String tradeId;
    private String tradeRef;
    private String tradedCcy;
    private String siteGroup;
    private String clientUuid;
    private String tradeUuid;
    private String settlementCcy;
    private String tradedQty;
    private String side;
    private String approvalRequestId;
    private String settlementQty;
}
