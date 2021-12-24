package bc.group.caspian.recon.api.platform.otcBookingTrades;

import lombok.*;

import java.util.List;


@Value
@Builder
@Data
public class OtcTradeBookingWithTimeRequest {

    String batchSize;
    String from;
    String to;
    String approvalStatus;
    List<String> classType;
    Long offset;
}
