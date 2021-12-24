package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesAccountBalance;
import bc.group.caspian.recon.api.platform.rfqTrades.PlatformRfqTrade;
import bc.group.caspian.recon.domain.mysql.PlatformRfqTradeEntity;
import bc.group.caspian.recon.domain.mysql.TransactionEntity;
import bc.group.caspian.recon.domain.reconInflux.PlatformRfqTradeMeasurement;
import bc.group.caspian.recon.domain.reconInflux.TransactionMeasurement;
import bc.group.caspian.recon.domain.reconInflux.accountbalance.OtherPendingAccountBalanceMeasurement;
import group.bc.caspian.connector.model.Transaction;
import lombok.Data;
import org.influxdb.InfluxDB;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest
class DataFeedServiceTest {

    @Measurement(name = "test_measurement")
    @Data
    public static class TestMeasurement {
        @Column(name = "test_field_a")
        private String testFieldA;
        @Column(name = "test_field_b")
        private Long testFieldB;
    }

    @Autowired
    private DataFeedService dataFeedService;

    @Autowired
    private InfluxDB influxDB;

    @Value("${spring.influx.database}")
    private String database;

    @BeforeEach
    public void beforeEach() {
        influxDB.query(new Query("DROP DATABASE " + database));
        influxDB.query(new Query("CREATE DATABASE " + database));
    }

    @Test
    @Disabled
    public void transactionDtoTest() {
        Transaction transaction = new Transaction();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        transaction.setFund("Fund OSLHK");
        transaction.setPortfolio("OTCFLOW");
        transaction.setSide("BUY");
        transaction.setCurrencyPair("UNI/USD");
        transaction.setOrderFeedId("order-feed");
        transaction.setQuantity(BigDecimal.valueOf(4.90));
        transaction.setTradeDay(LocalDate.parse("2019-09-20",df));
        transaction.setSettleDay(LocalDate.parse("2020-09-20",df));
        transaction.setExecutionTimestamp("2019-Sep-19 16:00:00 UTC");
        transaction.setInputTimestamp("2019-Sep-19 20:00:00 UTC");
        transaction.setUpdateTimestamp("2019-Sep-19 20:00:00 UTC");
        transaction.setPrice(BigDecimal.valueOf(1.00));
        transaction.setStrategy("OTCFLOW");
        transaction.setId("1e9df491-db5a-1140-bd0f-0289d35e2342");
        transaction.setAcquirer("VNTC");
        transaction.setCustodian("OSLBLOCKS");
        transaction.setTradeType("OTC Trade");
        transaction.setNetAmount(BigDecimal.valueOf(4.90));
        transaction.setGrossAmount(BigDecimal.valueOf(4.80));
        transaction.setCashEntry("1e9df491-db5c-1850-bd0f-0289d35e2342");
        transaction.setTradeType("CASH");
        transaction.setAccount("BTC");
        transaction.setCashEntry("1e9df491-db5c-1850-bd0f-0289d35e2342");

        Map<String,String> additionalData = new HashMap<>();
        additionalData.put("CptyOslUuid","10cdce81-3290-4a95-8910-9eb3fa4b4ff9");
        additionalData.put("SettleCcy","USD");
        additionalData.put("CptyCode","OS-BBR-22OCT19-211350-BTCHKD");

        Map<String, String> externalCode = new HashMap<>();
        externalCode.put("BLOOMBERG", "booking.rfq.1234");

        transaction.setAdditionalData(additionalData);
        transaction.setExternalCode(externalCode);

        TransactionMeasurement tm = dataFeedService.getTransactionDto(transaction,TransactionMeasurement.class);
        System.out.println(tm);
        assertEquals("USD", tm.getSettleCcy());

        assert  tm != null;

        TransactionEntity te = dataFeedService.getTransactionDto(transaction,TransactionEntity.class);
        System.out.println(te);

        assert  te != null;

        //TODO: problem on serializing localdatetime to json, may need to create a deserializer for it
        TransactionMeasurement tmFromTe = dataFeedService.getTransactionDto(te, TransactionMeasurement.class);
        System.out.println(tmFromTe);

        assert tmFromTe != null;

    }

    @Test
    @Disabled
    public void transactionDtoForPlatformRfqTradeEntityTest() {
        PlatformRfqTrade trade = new PlatformRfqTrade();
        trade.setTradeId("rfq-trade-0");
        trade.setTradeUuid("62ed1d35-cfcc-4e3b-95f9-1a41437ed17f");
        trade.setDateCreated("2021-04-23T15:07:19.000+08:00");
        trade.setLastUpdated("2021-04-23T16:17:45.000+08:00");
        trade.setBuyTradedCurrency(true);
        trade.setTradedCurrency("BTC");
        trade.setSettlementCurrency("USD");
        trade.setTradedCurrencyAmount("0.00906430");
        trade.setSettlementCurrencyAmount("450.00000000");
        trade.setForUsername("christine.sze+client@osl.com");
        trade.setForUserUuid("828093eb-cf24-42f2-85b3-b2fe3d84d476");
        trade.setSiteGroup("OSLLC_GROUP");
        trade.setTradeState("DVP_PTS_SETTLED");
        trade.setTreasuryUserUuid("7afe7ebc-d096-4be7-9580-2c36f118eb49");
        trade.setTreasuryUsername("prakash.konagi+treasury_ds@osl.com");
        trade.setTreasuryUserSiteGroup("OSLSG_GROUP");
        trade.setEnabledSimpleTradeWL(false);
        trade.setEnabledSimpleTradeWLSegWallet(true);

        String dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]";

        PlatformRfqTradeEntity entity = dataFeedService.getTransactionDto(trade, PlatformRfqTradeEntity.class);
        assertEquals(trade.getTradeId(), entity.getTradeId());
        assertEquals(ZonedDateTime.parse(trade.getLastUpdated(),
                DateTimeFormatter.ofPattern(dateTimePattern))
                        .withZoneSameInstant(ZoneOffset.UTC).toInstant().toString(),
                entity.getLastUpdated().toInstant().toString());
        assertEquals(ZonedDateTime.parse(trade.getDateCreated(),
                DateTimeFormatter.ofPattern(dateTimePattern))
                        .withZoneSameInstant(ZoneOffset.UTC).toInstant().toString(),
                entity.getDateCreated().toInstant().toString());
    }

    @Test
    @Disabled
    public void transactionDtoForPlatformRfqTradeMeasurementTest() {
        PlatformRfqTrade trade = new PlatformRfqTrade();
        trade.setTradeId("rfq-trade-0");
        trade.setTradeUuid("62ed1d35-cfcc-4e3b-95f9-1a41437ed17f");
        trade.setDateCreated("2021-04-23T15:07:19.000+08:00");
        trade.setLastUpdated("2021-04-23T16:17:45.000+08:00");
        trade.setBuyTradedCurrency(true);
        trade.setTradedCurrency("BTC");
        trade.setSettlementCurrency("USD");
        trade.setTradedCurrencyAmount("0.00906430");
        trade.setSettlementCurrencyAmount("450.00000000");
        trade.setForUsername("christine.sze+client@osl.com");
        trade.setForUserUuid("828093eb-cf24-42f2-85b3-b2fe3d84d476");
        trade.setSiteGroup("OSLLC_GROUP");
        trade.setTradeState("DVP_PTS_SETTLED");
        trade.setTreasuryUserUuid("7afe7ebc-d096-4be7-9580-2c36f118eb49");
        trade.setTreasuryUsername("prakash.konagi+treasury_ds@osl.com");
        trade.setTreasuryUserSiteGroup("OSLSG_GROUP");
        trade.setTreasuryUserSettlementCurrency("SUSD");
        trade.setTreasuryUserSettlementAmount("0.01");
        trade.setEnabledSimpleTradeWL(false);
        trade.setEnabledSimpleTradeWLSegWallet(true);

        PlatformRfqTradeMeasurement measurement = dataFeedService.getTransactionDto(trade, PlatformRfqTradeMeasurement.class);
        assertEquals(trade.getTradeId(), measurement.getTradeId());
        assertEquals(trade.getTradeUuid(), measurement.getTradeUuid());
        assertEquals(trade.getLastUpdated(),
                measurement.getLastUpdated());
        assertEquals(trade.getDateCreated(),
                measurement.getDateCreated());
        assertEquals(Boolean.toString(trade.getBuyTradedCurrency()), measurement.getBuyTradedCurrency());
        assertEquals(trade.getTradedCurrency(), measurement.getTradedCurrency());
        assertEquals(trade.getTradedCurrencyAmount(), measurement.getTradedCurrencyAmount());
        assertEquals(trade.getSettlementCurrency(), measurement.getSettlementCurrency());
        assertEquals(trade.getTradedCurrencyAmount(), measurement.getTradedCurrencyAmount());
        assertEquals(trade.getSettlementCurrencyAmount(), measurement.getSettlementCurrencyAmount());
        assertEquals(trade.getForUsername(), measurement.getForUsername());
        assertEquals(trade.getForUserUuid(), measurement.getForUserUuid());
        assertEquals(trade.getSiteGroup(), measurement.getSiteGroup());
        assertEquals(trade.getTradeState(), measurement.getTradeState());
        assertEquals(trade.getTreasuryUserUuid(), measurement.getTreasuryUserUuid());
        assertEquals(trade.getTreasuryUsername(), measurement.getTreasuryUsername());
        assertEquals(trade.getTreasuryUserSiteGroup(), measurement.getTreasuryUserSiteGroup());
        assertEquals(trade.getTreasuryUserSettlementCurrency(), measurement.getTreasuryUserSettlementCurrency());
        assertEquals(trade.getTreasuryUserSettlementAmount(), measurement.getTreasuryUserSettlementAmount());
        assertEquals(Boolean.toString(trade.getEnabledSimpleTradeWL()), measurement.getEnabledSimpleTradeWL());
        assertEquals(Boolean.toString(trade.getEnabledSimpleTradeWLSegWallet()), measurement.getEnabledSimpleTradeWLSegWallet());
    }

    @Test
    @Disabled
    public void transactionDtoForPlatformLedgerMeasurementTest() {
        LedgerBalancesAccountBalance ledgerBalancesAccountBalance = new LedgerBalancesAccountBalance();
        ledgerBalancesAccountBalance.setAvailableBalance(BigDecimal.valueOf(1L));
        ledgerBalancesAccountBalance.setBrokerage(BigDecimal.valueOf(2L));
        ledgerBalancesAccountBalance.setCcy("BCH");
        ledgerBalancesAccountBalance.setCollateral(BigDecimal.valueOf(3L));
        ledgerBalancesAccountBalance.setCredit(BigDecimal.valueOf(4L));
        ledgerBalancesAccountBalance.setExchangeAvailableBalance(BigDecimal.valueOf(5L));
        ledgerBalancesAccountBalance.setHold(BigDecimal.valueOf(6L));
        ledgerBalancesAccountBalance.setLeverageAvailableBalance(BigDecimal.valueOf(7L));
        ledgerBalancesAccountBalance.setOrder(BigDecimal.valueOf(8L));
        ledgerBalancesAccountBalance.setPendingWithdrawal(BigDecimal.valueOf(9L));
        ledgerBalancesAccountBalance.setSuspense(BigDecimal.valueOf(10L));
        ledgerBalancesAccountBalance.setUnconfirmed(BigDecimal.valueOf(11L));
        ledgerBalancesAccountBalance.setUnprocessedDeposit(BigDecimal.valueOf(12L));
        ledgerBalancesAccountBalance.setUnprocessedWithdrawal(BigDecimal.valueOf(13L));
        ledgerBalancesAccountBalance.setUnsettleBuy(BigDecimal.valueOf(14L));
        ledgerBalancesAccountBalance.setUnsettleSell(BigDecimal.valueOf(14L));

        OtherPendingAccountBalanceMeasurement measurement = dataFeedService.getTransactionDto(ledgerBalancesAccountBalance, OtherPendingAccountBalanceMeasurement.class);
        assertEquals(measurement.getCcy(), ledgerBalancesAccountBalance.getCcy());
        assertEquals(measurement.getBrokerage(), ledgerBalancesAccountBalance.getBrokerage().toString());
        assertEquals(measurement.getUnsettleBuy(),ledgerBalancesAccountBalance.getUnsettleBuy().toString());
        assertEquals(measurement.getUnsettleSell(),ledgerBalancesAccountBalance.getUnsettleSell().toString());
    }

    @Test
    @Disabled
    public void testPublish() {
        TestMeasurement testMeasurement = new TestMeasurement();
        testMeasurement.setTestFieldA("fieldA");
        testMeasurement.setTestFieldB(100L);
        dataFeedService.publishToInflux(testMeasurement);

        QueryResult queryResult = influxDB.query(BoundParameterQuery.QueryBuilder
                .newQuery("SELECT * FROM test_measurement")
                .forDatabase(database)
                .create());

        List<TestMeasurement> resultList = new InfluxDBResultMapper().toPOJO(
                influxDB.query(new Query("SELECT * FROM test_measurement", database)),
                TestMeasurement.class
        );
        assert 1 == resultList.size();
        TestMeasurement resultTestMeasurement = resultList.get(0);
        assert "fieldA".equals(resultTestMeasurement.getTestFieldA());
        assert 100L == resultTestMeasurement.getTestFieldB();
    }
}
