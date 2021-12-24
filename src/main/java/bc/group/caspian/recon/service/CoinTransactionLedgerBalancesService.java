package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.platform.coinTransactionLedgerBalances.CoinTransactionLedgerBalancesRequest;
import bc.group.caspian.recon.api.platform.coinTransactionLedgerBalances.CoinTransactionLedgerBalancesResponse;
import bc.group.caspian.recon.repository.ScheduledStatusRepository;
import bc.group.caspian.recon.service.api.platform.PlatformClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoinTransactionLedgerBalancesService {

    @Value("${txn-ledger-account-classes}")
    public List<String> accountClasses;

    private final PlatformClient platformClient;
    private final static Logger logger = LoggerFactory.getLogger(PlatformCoinTransactionService.class);


    public CoinTransactionLedgerBalancesService(PlatformClient platformClient, ScheduledStatusRepository scheduledStatusRepository) {
        this.platformClient = platformClient;
    }

    public CoinTransactionLedgerBalancesResponse getLedgerBalances() {
        CoinTransactionLedgerBalancesResponse response;
        try {
            CoinTransactionLedgerBalancesRequest request = CoinTransactionLedgerBalancesRequest.builder()
                    .accountClasses(accountClasses)
                    .build();

            response = platformClient.getTxnLedgerBalances(request);
            if (response.getResult() == null) {
                logger.info("Response was null trying to get response again");
                // Sleep for 1 min then again try to get response
                try {
                    Thread.sleep(60000);
                } catch (Exception e) {
                    logger.error("Error occurs when trying to sleep for 60s", e);
                }
                response = platformClient.getTxnLedgerBalances(request);
            }
        } catch (Exception e) {
            logger.error("Failed to get transaction ledger balances from api: %s", e);
            throw e;
        }
        return response;
    }
}
