package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesRequest;
import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesResponse;
import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesUser;
import bc.group.caspian.recon.domain.mysql.PlatformLedgerBalanceEntity;
import bc.group.caspian.recon.service.api.platform.PlatformClient;
import bc.group.caspian.recon.service.config.PlatformLedgerBalancesProperties;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PlatformLedgerBalanceService {

    private final PlatformClient platformClient;
    private final static Logger logger = LoggerFactory.getLogger(PlatformLedgerBalanceService.class);
    protected final PlatformLedgerBalancesProperties properties;

    public PlatformLedgerBalanceService(PlatformClient platformClient, PlatformLedgerBalancesProperties properties) {
        this.properties = properties;
        this.platformClient = platformClient;
    }

    public List<LedgerBalancesUser> getLedgerBalancesUsersByUuids(List<String> uuids) {

        List<List<String>> batchedUuids = Lists.partition(
                Lists.newArrayList(uuids), Integer.parseInt(properties.getBatchSize()));

        return batchedUuids.stream()
                .map(this::getLedgerBalancesUsersBatch)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<LedgerBalancesUser> getLedgerBalancesUsersBatch(List<String> uuids) {
        LedgerBalancesRequest request = getLedgerBalancesRequest(uuids);

        try {
            LedgerBalancesResponse response = platformClient.getLedgerBalances(request);

            if (response.getResult() != null) {
                List<LedgerBalancesUser> users = response.getResult().getUsers();
                logger.info("Got {} new batch of ledger balances users from Platform api", users.size());
                return users;
            } else {
                logger.info("Failed to process ledger balances, result is null");
                return null;
            }

        } catch (Exception e) {
            logger.error("Failed to get platform ledger balances : %s", e);
            throw e;
        }
    }

    private LedgerBalancesRequest getLedgerBalancesRequest(List<String> userUuids) {
        return LedgerBalancesRequest.builder()
                .currencies(properties.getSupported().getCurrencies())
                .userUuids(userUuids)
                .build();
    }
}
