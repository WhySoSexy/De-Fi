package bc.group.caspian.recon.job;

import bc.group.caspian.recon.service.PlatformLedgerBalancesEntityService;
import bc.group.caspian.recon.service.config.PlatformLedgerBalancesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PlatformLedgerBalancesResetJob {
    private final static Logger logger = LoggerFactory.getLogger(PlatformLedgerBalancesJob.class);

    private final PlatformLedgerBalancesEntityService platformLedgerBalancesEntityService;
    protected final PlatformLedgerBalancesProperties properties;

    public PlatformLedgerBalancesResetJob(
            PlatformLedgerBalancesEntityService platformLedgerBalancesEntityService, PlatformLedgerBalancesProperties balanceProperties
    ) {
        this.platformLedgerBalancesEntityService = platformLedgerBalancesEntityService;
        this.properties = balanceProperties;
    }

    public void runAccountBalancesJob() {
        process();
    }

    protected int process() {
        int count = 0;
        try {
            Set<String> uuids = this.properties.getSupported().getAccountUuids();
            resetStateForPlatformLedgerBalances(uuids);
            count = uuids.size();
            logger.info(
                    "Status was set to uncompleted for {} platform ledger balances",
                    uuids)
            ;
        } catch (Exception e) {
            logger.error("Failed to execute platform ledger balances reset job", e);
        }

        return count;
    }

    protected void resetStateForPlatformLedgerBalances(Set<String> uuids) {
        platformLedgerBalancesEntityService.updateCompletedStateForPlatformLedgerBalances(
                uuids, false
        );
    }

}
