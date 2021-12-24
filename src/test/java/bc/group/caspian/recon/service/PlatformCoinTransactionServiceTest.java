package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.platform.coinTransaction.PlatformCoinTransaction;
import bc.group.caspian.recon.api.platform.coinTransaction.PlatformCoinTransactionResponse;
import bc.group.caspian.recon.api.platform.coinTransaction.PlatformCoinTransactionResult;
import bc.group.caspian.recon.repository.ScheduledStatusRepository;
import bc.group.caspian.recon.service.api.platform.PlatformClient;
import bc.group.caspian.recon.service.config.PlatformCoinTransactionProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class PlatformCoinTransactionServiceTest {

    @Mock
    PlatformClient client;

    @Mock
    ScheduledStatusRepository repository;

    @InjectMocks
    PlatformCoinTransactionService service;

    @Mock
    PlatformCoinTransactionProperties properties;

    @Test
    public void getPlatformCoinTransactionTest() {

        PlatformCoinTransaction txn = new PlatformCoinTransaction();
        txn.setAmount("10");
        txn.setAccountUuid("89c1dee1-79bd-47bd-a982-ea8d51157fb4");
        txn.setCoinAddress("rU2mEJSLqBRkYLVTv55rFTgQajkLTnT6mA");
        txn.setFee("0.0");
        txn.setId("100");
        txn.setCcy("XRP");
        PlatformCoinTransactionResponse response = new PlatformCoinTransactionResponse();
        PlatformCoinTransactionResult res = new PlatformCoinTransactionResult();
        res.setOffset(0L);
        res.setTotalCount(1L);
        res.setTransactions(Collections.singletonList(txn));
        response.setResultCode("OK");
        response.setTimestamp("1612939977366");
        response.setResult(res);

        Mockito.when(client.getCoinTransactions(any())).thenReturn(response);
        Mockito.when(repository.findSchedulerStatusEntityByName(any())).thenReturn(null);
        Mockito.when(properties.getBatchSize()).thenReturn(10L);
        Mockito.when(properties.getStartDate()).thenReturn(1601510400000L);
        PlatformCoinTransactionResponse result = service.getPlatformCoinTransactions();
        PlatformCoinTransaction transaction = result.getResult().getTransactions().get(0);
        assertEquals(txn.getCcy(), transaction.getCcy());
        assertEquals(txn.getAccountUuid(), transaction.getAccountUuid());
        assertEquals(txn.getAmount(), transaction.getAmount());
    }
}
