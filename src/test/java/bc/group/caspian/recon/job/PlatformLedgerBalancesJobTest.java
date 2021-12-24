package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesAccountBalance;
import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesUser;
import bc.group.caspian.recon.domain.reconInflux.PlatformRfqTradeMeasurement;
import bc.group.caspian.recon.domain.reconInflux.accountbalance.*;
import bc.group.caspian.recon.service.DataFeedService;
import bc.group.caspian.recon.service.config.PlatformLedgerBalancesProperties;
import org.influxdb.annotation.Column;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class PlatformLedgerBalancesJobTest {

    @InjectMocks
    PlatformLedgerBalancesJob ledgerBalancesScheduler;

    @Mock
    DataFeedService dataFeedService;

    List<LedgerBalancesUser> ledgerBalancesUserAccountBalances;
    List<LedgerBalancesAccountBalance> balances;

    @BeforeEach
    public void setUp() {
        PlatformLedgerBalancesProperties.SupportedProperty.CounterpartyProperty counterpartyProperty =
                new PlatformLedgerBalancesProperties.SupportedProperty.CounterpartyProperty();
        counterpartyProperty.setSource("source-uuid-0");
        counterpartyProperty.setCounterparty("counterparty-uuid-0");

        PlatformLedgerBalancesProperties.SupportedProperty.OslsgsExcludedProperty oslsgsExcludedProperty =
                new PlatformLedgerBalancesProperties.SupportedProperty.OslsgsExcludedProperty();
        oslsgsExcludedProperty.setTradingspot("tradingspot-uuid-0");
        oslsgsExcludedProperty.setOslsgs("oslsgs-uuid-0");

        PlatformLedgerBalancesProperties.SupportedProperty supportedProperties = new PlatformLedgerBalancesProperties.SupportedProperty();
        supportedProperties.setCurrencies(Arrays.asList("currencies", "BTC"));
        supportedProperties.setOtherPendingUuids(Arrays.asList("other-pending-uuid-0", "other-pending-uuid-1"));
        supportedProperties.setCounterpartyUuids(Collections.singletonList(counterpartyProperty));
        supportedProperties.setOslsgsExcludedUuids(Collections.singletonList(oslsgsExcludedProperty));
        supportedProperties.setInhouseUuids(Arrays.asList("inhouse-uuid-0", "inhouse-uuid-1"));
        supportedProperties.setOslsgsPendingUuids(Arrays.asList("oslsg-pending-uuid-0", "oslsg-pending-uuid-1"));
        supportedProperties.setTreasuryUuids(Arrays.asList("treasury-uuid-0", "treasury-uuid-1"));
        supportedProperties.setTraderUuids(Arrays.asList("trader-uuid-0"));
        supportedProperties.setTraderPendingUuids(Arrays.asList("trader-pending-uuid-0"));

        PlatformLedgerBalancesProperties platformLedgerBalancesProperties = new PlatformLedgerBalancesProperties();
        platformLedgerBalancesProperties.setBatchSize("30");
        platformLedgerBalancesProperties.setSupported(supportedProperties);
        platformLedgerBalancesProperties.setCron("0 0 0,9,17 * * *");
        ReflectionTestUtils.setField(ledgerBalancesScheduler, "properties", platformLedgerBalancesProperties);

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

        LedgerBalancesAccountBalance ledgerBalancesAccountBalance1 = new LedgerBalancesAccountBalance();
        ledgerBalancesAccountBalance1.setAvailableBalance(BigDecimal.valueOf(16L));
        ledgerBalancesAccountBalance1.setBrokerage(BigDecimal.valueOf(17L));
        ledgerBalancesAccountBalance1.setCcy("USD");
        ledgerBalancesAccountBalance1.setCollateral(BigDecimal.valueOf(18L));
        ledgerBalancesAccountBalance1.setCredit(BigDecimal.valueOf(19L));
        ledgerBalancesAccountBalance1.setExchangeAvailableBalance(BigDecimal.valueOf(20L));
        ledgerBalancesAccountBalance1.setHold(BigDecimal.valueOf(21L));
        ledgerBalancesAccountBalance1.setLeverageAvailableBalance(BigDecimal.valueOf(22L));
        ledgerBalancesAccountBalance1.setOrder(BigDecimal.valueOf(23L));
        ledgerBalancesAccountBalance1.setPendingWithdrawal(BigDecimal.valueOf(24L));
        ledgerBalancesAccountBalance1.setSuspense(BigDecimal.valueOf(25L));
        ledgerBalancesAccountBalance1.setUnconfirmed(BigDecimal.valueOf(26L));
        ledgerBalancesAccountBalance1.setUnprocessedDeposit(BigDecimal.valueOf(27L));
        ledgerBalancesAccountBalance1.setUnprocessedWithdrawal(BigDecimal.valueOf(28L));
        ledgerBalancesAccountBalance1.setUnsettleBuy(BigDecimal.valueOf(29L));
        ledgerBalancesAccountBalance1.setUnsettleSell(BigDecimal.valueOf(30L));

        balances =  Arrays.asList(ledgerBalancesAccountBalance, ledgerBalancesAccountBalance1);

        // Other pending
        LedgerBalancesUser user2 = new LedgerBalancesUser();
        user2.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getOtherPendingUuids().get(0));
        user2.setUsername("other-pending-uuid-0");
        user2.setSiteGroup("OSL SG");
        user2.setAccountBalances(balances);

        LedgerBalancesUser user3 = new LedgerBalancesUser();
        user3.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getOtherPendingUuids().get(1));
        user3.setUsername("other-pending-uuid-1");
        user3.setSiteGroup("OSL SG");
        user3.setAccountBalances(balances);

        // Oslsgs pending
        LedgerBalancesUser user4 = new LedgerBalancesUser();
        user4.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getOslsgsPendingUuids().get(0));
        user4.setUsername("oslsgs-pending-uuid-0");
        user4.setSiteGroup("OSL SG");
        user4.setAccountBalances(balances);

        LedgerBalancesUser user5 = new LedgerBalancesUser();
        user5.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getOslsgsPendingUuids().get(1));
        user5.setUsername("oslsgs-pending-uuid-1");
        user5.setSiteGroup("OSL SG");
        user5.setAccountBalances(balances);

        // Inhouse
        LedgerBalancesUser user6 = new LedgerBalancesUser();
        user6.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getInhouseUuids().get(0));
        user6.setUsername("inhouse-uuid-0");
        user6.setSiteGroup("OSL SG");
        user6.setAccountBalances(balances);

        LedgerBalancesUser user7 = new LedgerBalancesUser();
        user7.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getInhouseUuids().get(1));
        user7.setUsername("inhouse-uuid-1");
        user7.setSiteGroup("OSL SG");
        user7.setAccountBalances(balances);

        // Treasury
        LedgerBalancesUser user8 = new LedgerBalancesUser();
        user8.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getTreasuryUuids().get(0));
        user8.setUsername("treasury-uuid-0");
        user8.setSiteGroup("OSL SG");
        user8.setAccountBalances(balances);

        LedgerBalancesUser user9 = new LedgerBalancesUser();
        user9.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getTreasuryUuids().get(1));
        user9.setUsername("treasury-uuid-1");
        user9.setSiteGroup("OSL SG");
        user9.setAccountBalances(balances);

        // Counterparty
        LedgerBalancesUser user10 = new LedgerBalancesUser();
        user10.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getCounterpartyUuids().get(0).getCounterparty());
        user10.setUsername("counterparty-uuid");
        user10.setSiteGroup("OSL SG");
        user10.setAccountBalances(balances);

        LedgerBalancesUser user11 = new LedgerBalancesUser();
        user11.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getCounterpartyUuids().get(0).getSource());
        user11.setUsername("source-uuid");
        user11.setSiteGroup("OSL SG");
        user11.setAccountBalances(balances);

        // Oslsgs excluded
        LedgerBalancesUser user12 = new LedgerBalancesUser();
        user12.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getOslsgsExcludedUuids().get(0).getTradingspot());
        user12.setUsername("tradingspot-uuid");
        user12.setSiteGroup("OSL SG");
        user12.setAccountBalances(balances);

        LedgerBalancesUser user13 = new LedgerBalancesUser();
        user13.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getOslsgsExcludedUuids().get(0).getOslsgs());
        user13.setUsername("oslsgs-uuid");
        user13.setSiteGroup("OSL SG");
        user13.setAccountBalances(balances);

        // Trader
        LedgerBalancesUser user14 = new LedgerBalancesUser();
        user14.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getTraderUuids().get(0));
        user14.setUsername("trader-uuid-0");
        user14.setSiteGroup("OSL SG");
        user14.setAccountBalances(balances);

        // Trader pending
        LedgerBalancesUser user15 = new LedgerBalancesUser();
        user15.setUserUuid(ledgerBalancesScheduler.properties.getSupported().getTraderPendingUuids().get(0));
        user15.setUsername("trader-pending-uuid-0");
        user15.setSiteGroup("OSL SG");
        user15.setAccountBalances(balances);

        ledgerBalancesUserAccountBalances = Arrays.asList(user2, user3, user4, user5,
                user6, user7, user8, user9, user10, user11, user12, user13, user14, user15
        );
    }

    @Test
    public void transformToLedgerBalanceReturnCorrectAmountOfMeasurementsTest() {
        LedgerBalancesAccountBalance ledgerBalancesAccountBalance = balances.get(0);

        Mockito.when(dataFeedService.getTransactionDto(Mockito.any(), Mockito.any()))
                .thenReturn(new TraderAccountBalanceMeasurement());
        List<LedgerAccountBalanceMeasurement> traderAccountBalanceMeasurements = ledgerBalancesScheduler.transformToLedgerBalanceMeasurements(
                ledgerBalancesUserAccountBalances, AccountBalanceType.TRADER
        );
        assertEquals(2, (long) traderAccountBalanceMeasurements.size());
        assertEquals(traderAccountBalanceMeasurements.get(0).getAccount(), "trader-uuid-0");

        List<LedgerAccountBalanceMeasurement> traderPendingAccountBalanceMeasurements = ledgerBalancesScheduler.transformToLedgerBalanceMeasurements(
                ledgerBalancesUserAccountBalances, AccountBalanceType.PENDING_TRADER
        );
        assertEquals(2, (long) traderPendingAccountBalanceMeasurements.size());
        assertEquals(traderPendingAccountBalanceMeasurements.get(0).getAccount(), "trader-pending-uuid-0");
    }

    @Test
    public void transformToLedgerBalanceMeasurementOtherPendingTypeTest() {
        LedgerBalancesUser user = ledgerBalancesUserAccountBalances.get(0);
        LedgerBalancesAccountBalance balance = user.getAccountBalances().get(0);
        LedgerBalancesAccountBalance balance1 = user.getAccountBalances().get(1);
        BigDecimal expBalance0 = balance.getUnsettleBuy().subtract(balance.getUnsettleSell());
        BigDecimal expBalance1 = balance1.getUnsettleBuy().subtract(balance1.getUnsettleSell());

        Mockito.when(dataFeedService.getTransactionDto(balance, OtherPendingAccountBalanceMeasurement.class))
                .thenReturn(new OtherPendingAccountBalanceMeasurement());
        Mockito.when(dataFeedService.getTransactionDto(balance1, OtherPendingAccountBalanceMeasurement.class))
                .thenReturn(new OtherPendingAccountBalanceMeasurement());

        List<LedgerAccountBalanceMeasurement> accountBalanceMeasurement = ledgerBalancesScheduler.transformToLedgerBalanceMeasurement(
                user, AccountBalanceType.PENDING_OTHER
        );

        assertEquals(user.getAccountBalances().size(), accountBalanceMeasurement.size());
        assertEquals(user.getUsername(), accountBalanceMeasurement.get(0).getAccount());
        assertEquals(expBalance0.toString(), accountBalanceMeasurement.get(0).getBalance());
        assertEquals(expBalance1.toString(), accountBalanceMeasurement.get(1).getBalance());
    }

    @Test
    public void transformToLedgerBalanceMeasurementOslsgsPendingTypeTest() {
        LedgerBalancesUser user = ledgerBalancesUserAccountBalances.get(0);
        LedgerBalancesAccountBalance balance = user.getAccountBalances().get(0);
        LedgerBalancesAccountBalance balance1 = user.getAccountBalances().get(1);
        BigDecimal expBalance0 = balance.getUnsettleSell().add(balance.getUnsettleBuy().negate());
        BigDecimal expBalance1 = balance1.getUnsettleSell().add(balance1.getUnsettleBuy().negate());

        Mockito.when(dataFeedService.getTransactionDto(balance, OslsgsPendingAccountBalanceMeasurement.class))
                .thenReturn(new OslsgsPendingAccountBalanceMeasurement());
        Mockito.when(dataFeedService.getTransactionDto(balance1, OslsgsPendingAccountBalanceMeasurement.class))
                .thenReturn(new OslsgsPendingAccountBalanceMeasurement());

        List<LedgerAccountBalanceMeasurement> accountBalanceMeasurement = ledgerBalancesScheduler.transformToLedgerBalanceMeasurement(
                user, AccountBalanceType.PENDING_OSLSGS
        );

        assertEquals(user.getAccountBalances().size(), accountBalanceMeasurement.size());
        assertEquals(user.getUsername(), accountBalanceMeasurement.get(0).getAccount());
        assertEquals(expBalance0.toString(), accountBalanceMeasurement.get(0).getBalance());
        assertEquals(expBalance1.toString(), accountBalanceMeasurement.get(1).getBalance());
    }

    @Test
    public void transformToLedgerBalanceMeasurementInhouseTypeTest() {
        LedgerBalancesUser user = ledgerBalancesUserAccountBalances.get(0);
        LedgerBalancesAccountBalance balance = user.getAccountBalances().get(0);
        LedgerBalancesAccountBalance balance1 = user.getAccountBalances().get(1);
        BigDecimal expBalance0 = balance.getAvailableBalance().add(balance.getBrokerage())
                .add(balance.getHold()).add(balance.getSuspense())
                .add(balance.getOrder()).add(balance.getPendingWithdrawal().negate())
                .add(balance.getExchangeAvailableBalance()).add(balance.getLeverageAvailableBalance())
                .add(balance.getCollateral()).add(balance.getUnprocessedDeposit())
                .add(balance.getUnprocessedWithdrawal()).add(balance.getUnconfirmed());
        Mockito.when(dataFeedService.getTransactionDto(balance, InhouseAccountBalanceMeasurement.class))
                .thenReturn(new InhouseAccountBalanceMeasurement());
        Mockito.when(dataFeedService.getTransactionDto(balance1, InhouseAccountBalanceMeasurement.class))
                .thenReturn(new InhouseAccountBalanceMeasurement());

        List<LedgerAccountBalanceMeasurement> accountBalanceMeasurement = ledgerBalancesScheduler.transformToLedgerBalanceMeasurement(
                user, AccountBalanceType.INHOUSE
        );

        assertEquals(user.getAccountBalances().size(), accountBalanceMeasurement.size());
        assertEquals(user.getUsername(), accountBalanceMeasurement.get(0).getAccount());
        assertEquals(expBalance0.toString(), accountBalanceMeasurement.get(0).getBalance());
    }

    @Test
    public void transformToLedgerBalanceMeasurementTreasuryTypeTest() {
        LedgerBalancesUser user = ledgerBalancesUserAccountBalances.get(0);
        LedgerBalancesAccountBalance balance = user.getAccountBalances().get(0);
        LedgerBalancesAccountBalance balance1 = user.getAccountBalances().get(1);

        BigDecimal expBalance0 = balance.getUnsettleBuy().subtract(balance.getUnsettleSell());

        Mockito.when(dataFeedService.getTransactionDto(balance, TreasuryAccountBalanceMeasurement.class))
                .thenReturn(new TreasuryAccountBalanceMeasurement());
        Mockito.when(dataFeedService.getTransactionDto(balance1, TreasuryAccountBalanceMeasurement.class))
                .thenReturn(new TreasuryAccountBalanceMeasurement());

        List<LedgerAccountBalanceMeasurement> accountBalanceMeasurement = ledgerBalancesScheduler.transformToLedgerBalanceMeasurement(
                user, AccountBalanceType.TREASURY
        );

        assertEquals(user.getAccountBalances().size(), accountBalanceMeasurement.size());
        assertEquals(user.getUsername(), accountBalanceMeasurement.get(0).getAccount());
        assertEquals(expBalance0.toString(), accountBalanceMeasurement.get(0).getBalance());
    }

    @Test
    public void transformToLedgerBalanceMeasurementTraderTypeTest() {
        LedgerBalancesUser user = ledgerBalancesUserAccountBalances.get(0);
        LedgerBalancesAccountBalance balance = user.getAccountBalances().get(0);
        LedgerBalancesAccountBalance balance1 = user.getAccountBalances().get(1);

        BigDecimal expBalance0 =  balance.getAvailableBalance()
                .add(balance.getBrokerage()).add(balance.getExchangeAvailableBalance())
                .add(balance.getCredit().negate()).add(balance.getUnprocessedDeposit())
                .add(balance.getUnprocessedWithdrawal()).add(balance.getHold()).add(balance.getSuspense());

        Mockito.when(dataFeedService.getTransactionDto(balance, TraderAccountBalanceMeasurement.class))
                .thenReturn(new TraderAccountBalanceMeasurement());
        Mockito.when(dataFeedService.getTransactionDto(balance1, TraderAccountBalanceMeasurement.class))
                .thenReturn(new TraderAccountBalanceMeasurement());

        List<LedgerAccountBalanceMeasurement> accountBalanceMeasurement = ledgerBalancesScheduler.transformToLedgerBalanceMeasurement(
                user, AccountBalanceType.TRADER
        );

        assertEquals(user.getAccountBalances().size(), accountBalanceMeasurement.size());
        assertEquals(user.getUsername(), accountBalanceMeasurement.get(0).getAccount());
        assertEquals(expBalance0.toString(), accountBalanceMeasurement.get(0).getBalance());
    }

    @Test
    public void transformToLedgerBalanceMeasurementTraderPendingTypeTest() {
        LedgerBalancesUser user = ledgerBalancesUserAccountBalances.get(0);
        LedgerBalancesAccountBalance balance = user.getAccountBalances().get(0);
        LedgerBalancesAccountBalance balance1 = user.getAccountBalances().get(1);

        BigDecimal expBalance0 =  balance.getAvailableBalance().add(balance.getOrder())
                .add(balance.getSuspense()).add(balance.getHold())
                .add(balance.getCredit()).add(balance.getPendingWithdrawal())
                .add(balance.getExchangeAvailableBalance()).add(balance.getLeverageAvailableBalance()
                        .add(balance.getUnsettleBuy()).subtract(balance.getUnsettleSell()));

        Mockito.when(dataFeedService.getTransactionDto(balance, TraderPendingAccountBalanceMeasurement.class))
                .thenReturn(new TraderPendingAccountBalanceMeasurement());
        Mockito.when(dataFeedService.getTransactionDto(balance1, TraderPendingAccountBalanceMeasurement.class))
                .thenReturn(new TraderPendingAccountBalanceMeasurement());

        List<LedgerAccountBalanceMeasurement> accountBalanceMeasurement = ledgerBalancesScheduler.transformToLedgerBalanceMeasurement(
                user, AccountBalanceType.PENDING_TRADER
        );

        assertEquals(user.getAccountBalances().size(), accountBalanceMeasurement.size());
        assertEquals(user.getUsername(), accountBalanceMeasurement.get(0).getAccount());
        assertEquals(expBalance0.toString(), accountBalanceMeasurement.get(0).getBalance());
    }

    @Test
    public void transformToCounterpartyBalancesTest() {
        LedgerBalancesAccountBalance ledgerBalancesAccountBalanceCounterpaty = balances.get(0);
        LedgerBalancesAccountBalance ledgerBalancesAccountBalanceSource = balances.get(0);

        BigDecimal expBalance0 = ledgerBalancesAccountBalanceCounterpaty.getUnsettleBuy().subtract(ledgerBalancesAccountBalanceCounterpaty.getUnsettleSell())
                .subtract(ledgerBalancesAccountBalanceSource.getUnsettleBuy()).add(ledgerBalancesAccountBalanceSource.getUnsettleSell());

        List<CounterpartyAccountBalanceMeasurement> accountBalanceMeasurements =
                ledgerBalancesScheduler.transformToCounterpartyBalances(ledgerBalancesUserAccountBalances);

        assertEquals(2, (long) accountBalanceMeasurements.size());
        assertEquals("counterparty-uuid", accountBalanceMeasurements.get(0).getAccount());
        assertEquals("counterparty-uuid", accountBalanceMeasurements.get(1).getAccount());
        assertEquals(expBalance0.toString(), accountBalanceMeasurements.get(0).getBalance());
    }

    @Test
    public void transformToExcludedOslsgsBalancesTest() {
        LedgerBalancesAccountBalance ledgerBalancesAccountBalanceTradingspot = balances.get(0);
        LedgerBalancesAccountBalance ledgerBalancesAccountBalanceOslsgs = balances.get(0);

        BigDecimal expBalance0 = ledgerBalancesAccountBalanceTradingspot.getUnsettleBuy().subtract(ledgerBalancesAccountBalanceTradingspot.getUnsettleSell())
                .subtract(ledgerBalancesAccountBalanceOslsgs.getUnsettleBuy()).add(ledgerBalancesAccountBalanceOslsgs.getUnsettleSell());

        List<ExcludedOslsgsPendingAccountBalanceMeasurement> accountBalanceMeasurements =
                ledgerBalancesScheduler.transformToExcludedOslsgsBalances(ledgerBalancesUserAccountBalances);

        assertEquals(2, (long) accountBalanceMeasurements.size());
        assertEquals("tradingspot-uuid", accountBalanceMeasurements.get(0).getAccount());
        assertEquals("tradingspot-uuid", accountBalanceMeasurements.get(1).getAccount());
        assertEquals(expBalance0.toString(), accountBalanceMeasurements.get(0).getBalance());
    }
}
