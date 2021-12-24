package bc.group.caspian.recon.domain.reconInflux;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Measurement(name = "transaction")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionMeasurement {

    @Column(name = "fund")
    private String fund;

    @Column(name = "portfolio")
    private String portfolio;

    @Column(name = "side")
    private String side;

    @Column(name = "quantity")
    private String quantity;

    @Column(name = "tradeDay")
    private String tradeDay;

    @Column(name = "settleDay")
    private String settleDay;

    //TODO: change string to timestamp?
    @Column(name = "executionTimestamp")
    private String executionTimestamp;

    @Column(name = "price")
    private String price;

    @Column(name = "strategy")
    private String strategy;

    @Column(name = "id")
    private String id;

    @Column(name = "acquirer")
    private String acquirer;

    @Column(name = "custodian")
    private String custodian;

    @Column(name = "tradeType")
    private String tradeType;

    @Column(name = "netAmount")
    private String netAmount;

    @Column(name = "grossAmount")
    private String grossAmount;

    @Column(name = "transactionType")
    private String transactionType;

    @Column(name = "inputTimestamp")
    private String inputTimestamp;

    @Column(name = "updateTimestamp")
    private String updateTimestamp;

    @Column(name = "account")
    private String account;

    @Column(name = "cashEntry")
    private String cashEntry;

    @Column(name = "instrument")
    private String instrument;

    @Column(name = "currency")
    @Setter(AccessLevel.NONE) private String currency;

    //From Map additionalData
    @Column(name = "CptyOslUuid")
    private String cptyOslUuid;

    @Column(name = "CptyCode")
    private String cptyCode;

    @Column(name = "orderFeedId")
    private String orderFeedId;

    @Column(name = "SettleCcy")
    @Setter(AccessLevel.NONE)  private String settleCcy;

    @Column(name = "rfqTradeRef")
    private String rfqTradeRef;

    @Column(name = "tradedCcy")
    @Setter(AccessLevel.NONE)  private String tradedCcy;

    @Column(name = "status")
    private String status;

    @JsonProperty("rate")
    private Map<String,String> rateMap;

    @JsonProperty("additionalData")
    private void mapAdditionalDataToColumnFields(Map<String, String> additionalData) {
        String cptyOslUuid = additionalData.get("CptyOslUuid");
        if (cptyOslUuid != null && !cptyOslUuid.contains("#N/A")) {
            setCptyOslUuid(cptyOslUuid);
        }
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


    @JsonProperty("tradeDay")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MMM-dd")
    private void convertTradeDayToTimestamp(LocalDate tradeDay) {
        setTradeDay(changeDateToTimestamp(tradeDay));
    }

    @JsonProperty("settleDay")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MMM-dd")
    private void convertSettleDayToTimestamp(LocalDate settleDay) {
        setSettleDay(changeDateToTimestamp(settleDay));
    }

    @JsonProperty("updateTimestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MMM-dd HH:mm:ss z")
    private void convertUpdateTimestamp(LocalDateTime timestamp) {
        setUpdateTimestamp(changeDateTimeToTimestamp(timestamp));
    }

    @JsonProperty("inputTimestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MMM-dd HH:mm:ss z")
    private void convertInputTimestamp(LocalDateTime timestamp) {
        setInputTimestamp(changeDateTimeToTimestamp(timestamp));
    }

    @JsonProperty("executionTimestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MMM-dd HH:mm:ss z")
    private void convertExecutionTimeStamp(LocalDateTime timestamp) {
        setExecutionTimestamp(changeDateTimeToTimestamp(timestamp));
    }
    private void checkAndChangeValidFields(String changeCurrencies){
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

    //precision to nanoseconds
    private static String changeDateToTimestamp(LocalDate date) {
        return String.valueOf(date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    //precision to nanoseconds
    private static String changeDateTimeToTimestamp(LocalDateTime dateTime) {
        return String.valueOf(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli());
    }
}
