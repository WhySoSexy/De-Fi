package bc.group.caspian.recon.domain.reconInflux.accountbalance;

import lombok.Data;

@Data
public abstract class LedgerAccountBalanceMeasurement {

    private String account;
    private String site;
    private String ccy;
    private String balance;
    private String hold;
    private String available;
    private String brokerage;
    private String exchange;
    private String suspense;
    private String order;
    private String credit;
    private String leverage;
    private String collateral;
    private String pendingWithdrawal;
    private String unprocessedDeposit;
    private String unprocessedWithdrawal;
    private String unconfirmed;
    private String unsettleBuy;
    private String unsettleSell;
}
