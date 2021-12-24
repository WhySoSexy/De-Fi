package bc.group.caspian.recon.job;

import bc.group.caspian.recon.service.PlatformLedgerBalancesEntityService;
import bc.group.caspian.recon.service.config.PlatformLedgerBalancesProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class PlatformLedgerBalancesResetJobTest {

    @InjectMocks
    PlatformLedgerBalancesResetJob platformLedgerBalancesResetJob;

    @Mock
    PlatformLedgerBalancesEntityService platformLedgerBalancesEntityService;

    @Mock
    PlatformLedgerBalancesProperties properties;

    @Test
    public void processIfUuidsAreEmptyTest() {
        PlatformLedgerBalancesProperties.SupportedProperty supportedProperty = new PlatformLedgerBalancesProperties.SupportedProperty();
        Mockito.when(properties.getSupported()).thenReturn(supportedProperty);
        assertEquals(0, platformLedgerBalancesResetJob.process());
    }

    @Test
    public void processIfUuidsAreNotEmptyTest() {
        PlatformLedgerBalancesProperties.SupportedProperty supportedProperty = new PlatformLedgerBalancesProperties.SupportedProperty();
        supportedProperty.setInhouseUuids(Arrays.asList("uuid1", "uuid2", "uuid3"));
        Mockito.when(properties.getSupported()).thenReturn(supportedProperty);
        assertEquals(3, platformLedgerBalancesResetJob.process());
    }
}
