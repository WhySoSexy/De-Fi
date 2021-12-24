package bc.group.caspian.recon.domain.reconInflux.accountbalance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name="account_balance_excluded_oslsgs_pending_trades")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExcludedOslsgsPendingAccountBalanceMeasurement extends AccountBalanceMeasurement {

    @Column(name = "account")
    private String account;

    @Column(name = "site")
    private String site;

    @Column(name = "ccy")
    private String ccy;

    @Column(name = "unsettled_balance")
    private String balance;

    @Column(name = "tradingspot_unsettleBuy")
    private String tradingspotUnsettleBuy;

    @Column(name = "tradingspot_unsettleSell")
    private String tradingspotUnsettleSell;

    @Column(name = "oslsgs_unsettleBuy")
    private String oslsgsUnsettleBuy;

    @Column(name = "oslsgs_unsettleSell")
    private String oslsgsUnsettleSell;
}