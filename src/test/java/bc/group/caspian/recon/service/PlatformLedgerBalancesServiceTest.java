package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesAccountBalance;
import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesResponse;
import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesResult;
import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesUser;
import bc.group.caspian.recon.service.api.platform.PlatformClient;
import bc.group.caspian.recon.service.config.PlatformLedgerBalancesProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class PlatformLedgerBalancesServiceTest {

    @InjectMocks
    PlatformLedgerBalanceService platformLedgerBalanceService;

    @Mock
    PlatformClient client;

    @Mock
    PlatformLedgerBalancesProperties properties;

    @Test
    @Disabled
    public void getLedgerBalancesUsersTest() {
        Mockito.when(properties.getBatchSize()).thenReturn("30");
        Mockito.when(properties.getCron()).thenReturn("0 0 0,9,17 * * *");

        PlatformLedgerBalancesProperties.SupportedProperty property =
                new PlatformLedgerBalancesProperties.SupportedProperty();
        property.setCurrencies(Arrays.asList("BTC", "USD"));
        property.setCounterpartyUuids(Collections.emptyList());
        property.setOslsgsExcludedUuids(Collections.emptyList());
        property.setOtherPendingUuids(Collections.emptyList());
        property.setCounterpartyUuids(Collections.emptyList());
        property.setOslsgsPendingUuids(Collections.emptyList());
        property.setInhouseUuids(Collections.singletonList("uuid-0"));

        Mockito.when(properties.getSupported())
                .thenReturn(property);

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

        LedgerBalancesUser expectedUser = new LedgerBalancesUser();
        expectedUser.setUserUuid("uuid-0");
        expectedUser.setUsername("uuid-0-account");
        expectedUser.setSiteGroup("OSL SG");
        expectedUser.setAccountBalances(Collections.singletonList(ledgerBalancesAccountBalance));

        LedgerBalancesResponse response = new LedgerBalancesResponse();
        LedgerBalancesResult res = new LedgerBalancesResult();
        res.setCount(1L);
        res.setUsers(Collections.singletonList(expectedUser));
        response.setResultCode("OK");
        response.setResult(res);

        Mockito.when(client.getLedgerBalances(any())).thenReturn(response);

        List<LedgerBalancesUser> resultUsers = platformLedgerBalanceService.getLedgerBalancesUsersByUuids(Collections.emptyList());
        assertEquals(1, resultUsers.size());

        LedgerBalancesUser resultUser = resultUsers.get(0);

        assertEquals(expectedUser.getUserUuid(), resultUser.getUserUuid());
        assertEquals(expectedUser.getSiteGroup(), resultUser.getSiteGroup());
        assertEquals(expectedUser.getUsername(), resultUser.getUsername());

        assertEquals(expectedUser.getAccountBalances().size(), resultUser.getAccountBalances().size());
    }
}
