package bc.group.caspian.recon.api.platform.hedgeTrades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformHedgeTrade {

    private String id;
    private String uuid;
    private String lastUpdated;
    private String tradedCurrency;
    private String settlementCurrency;
    private Boolean buyTradedCurrency;
    private List<PlatformHedgeMerchantTransaction> merchantTransactions;
    private List<PlatformHedgeFloatTransaction> floatTransactions;

    public Boolean isValidHedgeTrade() {
        return !(Strings.isNullOrEmpty(this.getId()) ||
                Strings.isNullOrEmpty(this.getUuid()) ||
                Strings.isNullOrEmpty(this.getLastUpdated()) ||
                Strings.isNullOrEmpty(this.getTradedCurrency()) ||
                Strings.isNullOrEmpty(this.getSettlementCurrency())) &&
                isValidFloatTransactions();
    }

    private Boolean isValidFloatTransactions() {
        if (this.getFloatTransactions().size() < 2) return false;

        for (PlatformHedgeFloatTransaction transaction : this.getFloatTransactions()) {
            if (Strings.isNullOrEmpty(transaction.getTransactionId()) ||
                    Strings.isNullOrEmpty(transaction.getOriginatingTransactionId()) ||
                    Strings.isNullOrEmpty(transaction.getCcy()) ||
                    Strings.isNullOrEmpty(transaction.getSiteGroup()) ||
                    Strings.isNullOrEmpty(transaction.getProcessedDateTime()) ||
                    Strings.isNullOrEmpty(transaction.getReceivedDateTime()) ||
                    Strings.isNullOrEmpty(transaction.getState()) ||
                    Strings.isNullOrEmpty(transaction.getTxnType()) ||
                    Strings.isNullOrEmpty(transaction.getUserUuid()) ||
                    Strings.isNullOrEmpty(transaction.getUsername()) ||
                    transaction.getAmount() == null) return false;
        }
        return true;
    }
}