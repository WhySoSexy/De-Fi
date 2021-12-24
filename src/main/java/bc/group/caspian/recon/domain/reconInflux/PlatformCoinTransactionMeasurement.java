package bc.group.caspian.recon.domain.reconInflux;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name="platform_coin_txn")
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformCoinTransactionMeasurement {
    @Column(name = "id")
    private String id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "version")
    private String version;

    @Column(name = "account_uuid")
    private String accountUuid;

    @Column(name = "user_uuid")
    private String userUuid;

    @Column(name = "site_group")
    private String siteGroup;

    @Column(name = "ccy")
    private String ccy;

    @Column(name = "amount")
    private String amount;

    @Column(name = "fee")
    private String fee;

    @Column(name = "network_fee")
    private String networkFee;

    @Column(name = "class_name")
    private String className;

    @Column(name = "coin_address")
    private String coinAddress;

    @Column(name = "coin_confirmation")
    private String coinConfirmation;

    @Column(name = "coin_transaction_id")
    private String coinTransactionId;

    @Column(name = "processed_date_time")
    private String processedDateTime;

    @Column(name = "received_date_time")
    private String receivedDateTime;

    @Column(name = "transaction_state")
    private String transactionState;

    @Column(name = "transaction_type")
    private String transactionType;
}
