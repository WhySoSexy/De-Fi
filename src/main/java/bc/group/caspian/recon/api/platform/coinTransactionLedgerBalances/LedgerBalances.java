package bc.group.caspian.recon.api.platform.coinTransactionLedgerBalances;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LedgerBalances {
    BigDecimal nonDs;
    BigDecimal ds;

    public LedgerBalances(BigDecimal nonDs, BigDecimal ds) {
        this.nonDs = nonDs;
        this.ds = ds;
    }
}
