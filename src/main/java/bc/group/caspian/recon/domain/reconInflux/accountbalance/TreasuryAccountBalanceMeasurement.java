package bc.group.caspian.recon.domain.reconInflux.accountbalance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name="account_balance_treasury")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TreasuryAccountBalanceMeasurement extends LedgerAccountBalanceMeasurement {
    @Column(name = "account")
    private String account;

    @Column(name = "site")
    private String site;

    @Column(name = "ccy")
    private String ccy;

    @Column(name = "balance")
    private String balance;

    @Column(name = "hold")
    private String hold;

    @Column(name = "available")
    private String available;

    @Column(name = "brokerage")
    private String brokerage;

    @Column(name = "exchange")
    private String exchange;

    @Column(name = "suspense")
    private String suspense;

    @Column(name = "order")
    private String order;

    @Column(name = "credit")
    private String credit;

    @Column(name = "leverage")
    private String leverage;

    @Column(name = "pendingWithdrawal")
    private String pendingWithdrawal;

    @Column(name = "unprocessedDeposit")
    private String unprocessedDeposit;

    @Column(name = "unprocessedWithdrawal")
    private String unprocessedWithdrawal;
}
