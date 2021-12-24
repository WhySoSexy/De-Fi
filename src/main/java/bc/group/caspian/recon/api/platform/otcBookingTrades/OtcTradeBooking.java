package bc.group.caspian.recon.api.platform.otcBookingTrades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OtcTradeBooking {

    private Boolean buy;
    private String tradeDate;
    private String source;
    private String tradeId;
    private String tradeRef;
    private String tradeUuid;
    private String tradedCcy;
    private String siteGroup;
    private String clientUuid;
    private String settlementCcy;
    private BigDecimal tradedQty;
    private BigDecimal settlementQty;
    private String approvalRequestId;
}
