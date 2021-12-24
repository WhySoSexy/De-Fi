package bc.group.caspian.recon.domain.mysql;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Data
@Table(name = "transaction")
@IdClass(TransactionEntityPK.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {

    @Id
    @Column(length = 40)
    private String id;

    @Id
    @Column(length = 40)
    private String cashEntry;

    @Column(name = "inserted", nullable = false)
    private boolean inserted;

    @Column(name = "fund")
    private String fund;

    @Column(name = "portfolio")
    private String portfolio;

    @Column(name = "side")
    private String side;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "tradeDay")
    private String tradeDay;

    @Column(name = "settleDay")
    private String settleDay;

    @Column(name = "executionTimestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MMM-dd HH:mm:ss z", timezone = "UTC")
    private ZonedDateTime executionTimestamp;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "strategy")
    private String strategy;

    @Column(name = "acquirer")
    private String acquirer;

    @Column(name = "custodian")
    private String custodian;

    @Column(name = "tradeType")
    private String tradeType;

    @Column(name = "netAmount", nullable = false)
    private BigDecimal netAmount;

    @Column(name = "grossAmount", nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "transactionType")
    private String transactionType;

    @Column(name = "inputTimestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MMM-dd HH:mm:ss z", timezone = "UTC")
    private ZonedDateTime inputTimestamp;

    @Column(name = "updateTimestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MMM-dd HH:mm:ss z", timezone = "UTC")
    private ZonedDateTime updateTimestamp;

    @Column(name = "account")
    private String account;

    @Column(name = "instrument")
    private String instrument;

    @Column(name = "currency", nullable = false)
    @Setter(AccessLevel.NONE) private String currency;

    //From Map additionalData
    @Column(name = "CptyOslUuid")
    private String cptyOslUuid;

    @Column(name = "CptyCode")
    private String cptyCode;

    @Column(name = "SettleCcy")
    @Setter(AccessLevel.NONE)  private String settleCcy;

    @Column(name = "tradedCcy")
    @Setter(AccessLevel.NONE)  private String tradedCcy;

    @Column(name = "rfq_trade_ref")
    private String rfqTradeRef;

    @Column(name = "status")
    private String status;

    @Column(name = "orderFeedId")
    private String orderFeedId;

    public boolean getInserted() {
        return inserted;
    }

    @JsonProperty("additionalData")
    private void mapAdditionalDataToColumnFields(Map<String, String> additionalData) {
        setCptyOslUuid(additionalData.get("CptyOslUuid"));
        setCptyCode(additionalData.get("CptyCode"));
        setSettleCcy(additionalData.get("SettleCcy"));
    }

    @JsonProperty("externalCode")
    private void mapRfqTradeRef(Map<String, String> externalCode) {
        String bloomberg = externalCode.get("BLOOMBERG");
        if (bloomberg != null) {
            String [] bloombergSplit = bloomberg.split("\\.");
            if (bloombergSplit.length == 3 && bloombergSplit[2].matches("\\d+"))
                setRfqTradeRef(bloombergSplit[2]);

        }
    }

    @JsonProperty("currencyPair")
    private void mapСurrencyPair(String currencyPair) {
        if (currencyPair == null) return;
        String[] currencies = currencyPair.split("/");
        if (currencies.length == 2) {
            setTradedCcy(currencies[0]);
            setSettleCcy(currencies[1]);
        }
    }

    private  void checkAndChangeValidFields(String changeCurrencies){
        boolean aNull = changeCurrencies == null || changeCurrencies.trim().isEmpty() || changeCurrencies.equals("null");
        boolean checkBNP = changeCurrencies != null && changeCurrencies.equals("BNP");
        boolean checkDIN = changeCurrencies != null && changeCurrencies.equals("DIN");
        if (aNull){
            this.currency = "null";
        }
        else {
            this.currency = changeCurrencies;
        }

        if (aNull){
            this.tradedCcy = "null";
        }
        else {
            this.tradedCcy = changeCurrencies;
        }
        if (aNull){
            this.settleCcy = "null";
        }
        else {
            this.settleCcy = changeCurrencies;
        }

        if(checkBNP) {
           this.currency = "BAND";
        }
        if(checkDIN) {
            this.currency = "DAI";
        }
        if(checkBNP) {
            this.tradedCcy = "BAND";
        }
        if(checkDIN) {
            this.tradedCcy = "DAI";
        }
        if(checkBNP) {
            this.settleCcy = "BAND";
        }
        if(checkDIN) {
            this.settleCcy = "DAI";
        }

    }

    public void setCurrency(String currency) {
        checkAndChangeValidFields(currency);
    }

    public void setTradedCcy(String tradedCcy) {
        checkAndChangeValidFields(tradedCcy);
    }

    public void setSettleCcy(String settleCcy) {
        checkAndChangeValidFields(settleCcy);
    }
}
