package bc.group.caspian.recon.domain.reconInflux;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name="coin_txn_ledger_balances")
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinTransactionLedgerBalancesMeasurement {

    @Column(name = "ccy", tag = true)
    private String ccy;

    @Column(name = "non_ds_balances")
    private String nonDsBalances;

    @Column(name = "ds_balances")
    private String dsBalances;

}
