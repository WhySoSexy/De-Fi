package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.platform.coinTransactionLedgerBalances.CoinTransactionLedgerBalancesResponse;
import bc.group.caspian.recon.api.platform.coinTransactionLedgerBalances.LedgerBalances;
import bc.group.caspian.recon.domain.reconInflux.CoinTransactionLedgerBalancesMeasurement;
import bc.group.caspian.recon.service.CoinTransactionLedgerBalancesService;
import bc.group.caspian.recon.service.DataFeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;


@Service
public class CoinTransactionLedgerBalancesJob {
    private final DataFeedService dataFeedService;
    private final CoinTransactionLedgerBalancesService coinTransactionLedgerBalancesService;
    private final static Logger logger = LoggerFactory.getLogger(CoinTransactionLedgerBalancesJob.class);

    @Value("${txn-ledger-ds-site-group}")
    private List<String> dsSiteGroups;

    @Value("${txn-ledger-site-groups-disabled}")
    private List<String> disabledSiteGroup;

    HashMap<String, LedgerBalances> oldCcyBalancesList = new HashMap<>();

    public CoinTransactionLedgerBalancesJob(DataFeedService dataFeedService, CoinTransactionLedgerBalancesService coinTransactionLedgerBalancesService) {
        this.dataFeedService = dataFeedService;
        this.coinTransactionLedgerBalancesService = coinTransactionLedgerBalancesService;
    }

    public void runTxnGetLedgerBalanceJob() {
        try {
            CoinTransactionLedgerBalancesResponse response = coinTransactionLedgerBalancesService.getLedgerBalances();

            if (response.getResult() != null) {
                HashMap<String, LedgerBalances> ccyBalancesList = processResponse(response);
                logger.info("Got balances of {} currencies from Platform Ledger api", ccyBalancesList.entrySet().size());
                if (ccyBalancesList.size() > 0) {
                    process(ccyBalancesList);
                    setOldCcyBalances(ccyBalancesList);
                }
            } else {
                logger.info("Failed to process transaction ledger balances, received response is null");
            }
        } catch (Exception e) {
            logger.error("Failed to execute transaction ledger balances job", e);
        }
    }

    private void process(HashMap<String, LedgerBalances> ccyBalancesList) {
        try {
            for (String key : ccyBalancesList.keySet()) {
                if (oldCcyBalancesList.containsKey(key)) {
                    LedgerBalances oldBalance = oldCcyBalancesList.get(key);
                    LedgerBalances newBalance = ccyBalancesList.get(key);
                    if (!(newBalance.getDs().equals(oldBalance.getDs())) || !(newBalance.getNonDs().equals(oldBalance.getNonDs()))) {
                        saveToInflux(key, ccyBalancesList.get(key));
                    }
                } else {
                    saveToInflux(key, ccyBalancesList.get(key));
                }
            }

        } catch (Exception e) {
            logger.error("Failed to publish balance to influx", e);
            throw e;
        }
    }

    private void saveToInflux(String ccy, LedgerBalances balances) {
        try {
            CoinTransactionLedgerBalancesMeasurement measurement = CoinTransactionLedgerBalancesMeasurement.builder()
                    .ccy(ccy)
                    .nonDsBalances(balances.getNonDs().stripTrailingZeros().toPlainString())
                    .dsBalances(balances.getDs().stripTrailingZeros().toPlainString())
                    .build();
            dataFeedService.publishToInflux(measurement);
        } catch (Exception e) {
            logger.error("Failed to publish balance to influx for {} ", ccy, e);
            throw e;
        }
    }

    private HashMap<String, LedgerBalances> processResponse(CoinTransactionLedgerBalancesResponse response) {
        HashMap<String, LedgerBalances> ccyBalancesList = new HashMap<>();

        for (String siteGroup : response.getResult().keySet()) {
            if (disabledSiteGroup != null && disabledSiteGroup.contains(siteGroup)) {
                continue;
            }

            boolean isDsSiteGroup = dsSiteGroups != null ? dsSiteGroups.contains(siteGroup) : false;
            for (String accountClass : response.getResult().get(siteGroup).keySet()) {

                for (String ccy : response.getResult().get(siteGroup).get(accountClass).keySet()) {

                    if (!ccyBalancesList.containsKey(ccy)) {
                        BigDecimal balance = new BigDecimal(response.getResult().get(siteGroup).get(accountClass).get(ccy));
                        BigDecimal dsValue = isDsSiteGroup ? balance : BigDecimal.ZERO;
                        BigDecimal nonDsValue = isDsSiteGroup ? BigDecimal.ZERO : balance;
                        ccyBalancesList.put(ccy, new LedgerBalances(nonDsValue, dsValue));
                    } else {
                        BigDecimal preValueNonDs = ccyBalancesList.get(ccy).getNonDs();
                        BigDecimal preValueDs = ccyBalancesList.get(ccy).getDs();
                        BigDecimal balance = new BigDecimal(response.getResult().get(siteGroup).get(accountClass).get(ccy));
                        if (isDsSiteGroup) {
                            ccyBalancesList.put(ccy, new LedgerBalances(preValueNonDs, preValueDs.add(balance)));
                        } else {
                            ccyBalancesList.put(ccy, new LedgerBalances(preValueNonDs.add(balance), preValueDs));
                        }
                    }
                }
            }
        }
        return ccyBalancesList;

    }

    public void setOldCcyBalances(HashMap<String, LedgerBalances> ccyBalanceList) {
        oldCcyBalancesList.putAll(ccyBalanceList);
    }


}
