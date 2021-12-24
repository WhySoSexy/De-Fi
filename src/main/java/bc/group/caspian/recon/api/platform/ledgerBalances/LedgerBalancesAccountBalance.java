package bc.group.caspian.recon.api.platform.ledgerBalances;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LedgerBalancesAccountBalance {

    private BigDecimal availableBalance;

    private BigDecimal brokerage;

    private String ccy;

    private BigDecimal collateral;

    private BigDecimal credit;

    private BigDecimal exchangeAvailableBalance;

    private BigDecimal hold;

    private BigDecimal leverageAvailableBalance;

    private BigDecimal order;

    private BigDecimal pendingWithdrawal;

    private BigDecimal suspense;

    private BigDecimal unconfirmed;

    private BigDecimal unprocessedDeposit;

    private BigDecimal unprocessedWithdrawal;

    private BigDecimal unsettleBuy;

    private BigDecimal unsettleSell;
}
