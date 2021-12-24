package bc.group.caspian.recon.domain.reconInflux.accountbalance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name="account_balance_counterparty_pending_trades")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CounterpartyAccountBalanceMeasurement extends AccountBalanceMeasurement {

    @Column(name = "account")
    private String account;

    @Column(name = "site")
    private String site;

    @Column(name = "ccy")
    private String ccy;

    @Column(name = "unsettle_buy_balance")
    private String balance;

    @Column(name = "counterparty_unsettleBuy")
    private String counterpartyUnsettleBuy;

    @Column(name = "counterparty_unsettleSell")
    private String counterpartyUnsettleSell;

    @Column(name = "source_unsettleBuy")
    private String sourceUnsettleBuy;

    @Column(name = "source_unsettleSell")
    private String sourceUnsettleSell;
}
