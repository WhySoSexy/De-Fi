package bc.group.caspian.recon.job;

import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesAccountBalance;
import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesUser;
import bc.group.caspian.recon.domain.reconInflux.accountbalance.*;
import bc.group.caspian.recon.service.PlatformLedgerBalanceService;
import bc.group.caspian.recon.service.DataFeedService;
import bc.group.caspian.recon.service.PlatformLedgerBalancesEntityService;
import bc.group.caspian.recon.service.config.PlatformLedgerBalancesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlatformLedgerBalancesJob {

    private final DataFeedService dataFeedService;
    private final PlatformLedgerBalanceService platformLedgerBalancesService;
    private final PlatformLedgerBalancesEntityService platformLedgerBalancesEntityService;

    private final static Logger logger = LoggerFactory.getLogger(PlatformLedgerBalancesJob.class);

    protected final PlatformLedgerBalancesProperties properties;

    public PlatformLedgerBalancesJob(
            DataFeedService dataFeedService, PlatformLedgerBalanceService platformLedgerBalancesService,
            PlatformLedgerBalancesEntityService platformLedgerBalancesEntityService,
            PlatformLedgerBalancesProperties balanceProperties
    ) {
        this.dataFeedService = dataFeedService;
        this.platformLedgerBalancesService = platformLedgerBalancesService;
        this.platformLedgerBalancesEntityService = platformLedgerBalancesEntityService;
        this.properties = balanceProperties;
    }

    public void runAccountBalancesJob() {
        try {
            List<String> uuids = platformLedgerBalancesEntityService.findUncompletedUserIds();
            logger.info(
                    "Got {} ledger balances uuids {} to be processed",
                    uuids.size(), uuids);
            if (uuids.size() == 0) return;
            List<LedgerBalancesUser> users = platformLedgerBalancesService
                    .getLedgerBalancesUsersByUuids(uuids);
            logger.info(
                    "Got {} new ledger balances from Platform api",
                    users.size());
            completeStateForPlatformLedgerBalances(users);
            process(users);
        } catch (Exception e) {
            logger.error("Failed to execute account balances job", e);
        }
    }

    private void process(List<LedgerBalancesUser> ledgerBalancesUsers) {
        process(ledgerBalancesUsers, AccountBalanceType.INHOUSE);
        process(ledgerBalancesUsers, AccountBalanceType.PENDING_OSLSGS);
        process(ledgerBalancesUsers, AccountBalanceType.PENDING_OTHER);
        process(ledgerBalancesUsers, AccountBalanceType.TREASURY);
        process(ledgerBalancesUsers, AccountBalanceType.TRADER);
        process(ledgerBalancesUsers, AccountBalanceType.PENDING_TRADER);
        process(ledgerBalancesUsers, AccountBalanceType.TRADE_AHEAD);
        processCounterparty(ledgerBalancesUsers);
        processExcludedOslsgs(ledgerBalancesUsers);
    }

    private void process(List<LedgerBalancesUser> ledgerBalancesUsers, AccountBalanceType type) {
        List<LedgerAccountBalanceMeasurement> measurements = transformToLedgerBalanceMeasurements(ledgerBalancesUsers, type);
        try {
            publishToInflux(measurements);
            logger.info("{} {} account balances push into influx", measurements.size(), type);
        } catch (Exception e) {
            logger.info("Failed to push {} account balances into influx", type, e);
        }
    }

    protected List<LedgerAccountBalanceMeasurement> transformToLedgerBalanceMeasurements(
            List<LedgerBalancesUser> users, AccountBalanceType type
    ) {
        List<String> uuids = properties.getSupported().getUuids(type);
        return users.stream()
                .filter(b -> uuids.contains(b.getUserUuid()))
                .map(b -> transformToLedgerBalanceMeasurement(b, type))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    protected List<LedgerAccountBalanceMeasurement> transformToLedgerBalanceMeasurement(
            LedgerBalancesUser ledgerBalancesUser, AccountBalanceType type
    ) {
        return ledgerBalancesUser.getAccountBalances().stream()
                .map(b -> transformToLedgerBalanceMeasurement(ledgerBalancesUser, b, type))
                .collect(Collectors.toList());
    }

    private LedgerAccountBalanceMeasurement transformToLedgerBalanceMeasurement(
           LedgerBalancesUser user, LedgerBalancesAccountBalance accountBalance, AccountBalanceType type
    ) {
        LedgerAccountBalanceMeasurement measurement = createLedgerAccountBalanceMeasurement(accountBalance, type);
        BigDecimal balance = getAccountBalance(type, accountBalance);
        measurement.setBalance(balance.toString());
        measurement.setUnsettleSell(accountBalance.getUnsettleSell().negate().toString());
        measurement.setAccount(user.getUsername());
        measurement.setSite(user.getSiteGroup());

        return measurement;
    }

    private LedgerAccountBalanceMeasurement createLedgerAccountBalanceMeasurement(
            LedgerBalancesAccountBalance accountBalance, AccountBalanceType type
    ) {
        switch (type) {
            case PENDING_OSLSGS:
                return dataFeedService.getTransactionDto(accountBalance, OslsgsPendingAccountBalanceMeasurement.class);
            case PENDING_OTHER:
                return dataFeedService.getTransactionDto(accountBalance, OtherPendingAccountBalanceMeasurement.class);
            case PENDING_TRADER:
                return dataFeedService.getTransactionDto(accountBalance, TraderPendingAccountBalanceMeasurement.class);
            case TREASURY:
                return dataFeedService.getTransactionDto(accountBalance, TreasuryAccountBalanceMeasurement.class);
            case TRADER:
                return dataFeedService.getTransactionDto(accountBalance, TraderAccountBalanceMeasurement.class);
            case TRADE_AHEAD:
                return dataFeedService.getTransactionDto(accountBalance, TradeAheadAccountBalanceMeasurement.class);
            default:
                return dataFeedService.getTransactionDto(accountBalance, InhouseAccountBalanceMeasurement.class);
        }
    }

    private void processCounterparty(List<LedgerBalancesUser> ledgerBalancesUserAccountBalances) {
        List<CounterpartyAccountBalanceMeasurement> measurements = transformToCounterpartyBalances(ledgerBalancesUserAccountBalances);
        try {
            publishToInflux(measurements);
            logger.info("{} counterparty account balances push into influx", measurements.size());
        } catch (Exception e) {
            logger.info("Failed to push counterparty account balances into influx", e);
        }
    }

    protected List<CounterpartyAccountBalanceMeasurement> transformToCounterpartyBalances(
            List<LedgerBalancesUser> ledgerBalancesUserAccountBalances
    ) {
        return properties.getSupported().getCounterpartyUuids()
                .stream()
                .map(p -> {
                    LedgerBalancesUser source = ledgerBalancesUserAccountBalances
                            .stream()
                            .filter(b -> b.getUserUuid().equals(p.getSource()))
                            .findFirst()
                            .orElse(null);
                    LedgerBalancesUser counterparty = ledgerBalancesUserAccountBalances
                            .stream()
                            .filter(b -> b.getUserUuid().equals(p.getCounterparty()))
                            .findFirst()
                            .orElse(null);
                    if (source == null || counterparty == null) return null;

                    return transformToCounterpartyBalances(source, counterparty);
                })
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<CounterpartyAccountBalanceMeasurement> transformToCounterpartyBalances(
            LedgerBalancesUser source, LedgerBalancesUser counterparty
    ) {
        return source.getAccountBalances().stream().map(s -> {
                    String ccy = s.getCcy();
                    LedgerBalancesAccountBalance c = counterparty.getAccountBalances()
                            .stream()
                            .filter(e -> e.getCcy().equals(ccy))
                            .findFirst()
                            .orElse(null);

                    if (c == null) return null;
                    return transformToCounterpartyBalance(s, c, counterparty.getSiteGroup(), counterparty.getUsername());
                }
        )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private CounterpartyAccountBalanceMeasurement transformToCounterpartyBalance(
            LedgerBalancesAccountBalance source, LedgerBalancesAccountBalance counterparty, String siteGroup, String account
    ) {
        CounterpartyAccountBalanceMeasurement measurement = new CounterpartyAccountBalanceMeasurement();

        BigDecimal balance = counterparty.getUnsettleBuy().subtract(counterparty.getUnsettleSell())
                .subtract(source.getUnsettleBuy()).add(source.getUnsettleSell());
        measurement.setBalance(balance.toString());
        measurement.setCcy(counterparty.getCcy());
        measurement.setAccount(account);
        measurement.setSite(siteGroup);
        measurement.setCounterpartyUnsettleSell(counterparty.getUnsettleSell().toString());
        measurement.setCounterpartyUnsettleBuy(counterparty.getUnsettleBuy().toString());
        measurement.setSourceUnsettleSell(source.getUnsettleSell().toString());
        measurement.setSourceUnsettleBuy(source.getUnsettleBuy().toString());

        return measurement;
    }

    private void processExcludedOslsgs(List<LedgerBalancesUser> ledgerBalancesUserAccountBalances) {
        List<ExcludedOslsgsPendingAccountBalanceMeasurement> measurements = transformToExcludedOslsgsBalances(ledgerBalancesUserAccountBalances);
        try {
            publishToInflux(measurements);
            logger.info("{} excluded oslsgs account balances push into influx", measurements.size());
        } catch (Exception e) {
            logger.info("Failed to push excluded oslsgs account balances into influx", e);
        }
    }

    protected List<ExcludedOslsgsPendingAccountBalanceMeasurement> transformToExcludedOslsgsBalances(
            List<LedgerBalancesUser> ledgerBalancesUserAccountBalances
    ) {
        return properties.getSupported().getOslsgsExcludedUuids()
                .stream()
                .map(p -> {
                    LedgerBalancesUser tradingSpot = ledgerBalancesUserAccountBalances
                            .stream()
                            .filter(b -> b.getUserUuid().equals(p.getTradingspot()))
                            .findFirst()
                            .orElse(null);
                    LedgerBalancesUser oslsgs = ledgerBalancesUserAccountBalances
                            .stream()
                            .filter(b -> b.getUserUuid().equals(p.getOslsgs()))
                            .findFirst()
                            .orElse(null);
                    if (tradingSpot == null || oslsgs == null) return null;

                    return transformToExcludedOslsgsBalances(tradingSpot, oslsgs);
                })
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<ExcludedOslsgsPendingAccountBalanceMeasurement> transformToExcludedOslsgsBalances(
            LedgerBalancesUser tradingspot, LedgerBalancesUser oslsgs
    ) {
        return tradingspot.getAccountBalances().stream().map(t -> {
                    String ccy = t.getCcy();
                    LedgerBalancesAccountBalance o = oslsgs.getAccountBalances()
                            .stream()
                            .filter(e -> e.getCcy().equals(ccy))
                            .findFirst()
                            .orElse(null);

                    if (o == null) return null;
                    return transformToOslsgsBalance(t, o, tradingspot.getSiteGroup(), tradingspot.getUsername());
                }
        )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ExcludedOslsgsPendingAccountBalanceMeasurement transformToOslsgsBalance(
            LedgerBalancesAccountBalance tradingspotAccount, LedgerBalancesAccountBalance oslsgsAccount, String siteGroup, String account
    ) {
        ExcludedOslsgsPendingAccountBalanceMeasurement measurement = new ExcludedOslsgsPendingAccountBalanceMeasurement();

        BigDecimal balance = tradingspotAccount.getUnsettleBuy().subtract(tradingspotAccount.getUnsettleSell())
                .subtract(oslsgsAccount.getUnsettleBuy()).add(oslsgsAccount.getUnsettleSell());
        measurement.setBalance(balance.toString());
        measurement.setAccount(account);
        measurement.setSite(siteGroup);
        measurement.setCcy(tradingspotAccount.getCcy());
        measurement.setTradingspotUnsettleSell(tradingspotAccount.getUnsettleSell().toString());
        measurement.setTradingspotUnsettleBuy(tradingspotAccount.getUnsettleBuy().toString());
        measurement.setOslsgsUnsettleSell(oslsgsAccount.getUnsettleSell().toString());
        measurement.setOslsgsUnsettleSell(oslsgsAccount.getUnsettleBuy().toString());

        return measurement;
    }

    private BigDecimal getAccountBalance(AccountBalanceType type, LedgerBalancesAccountBalance ledgerBalancesAccountBalance) {
        switch (type) {
            case PENDING_OSLSGS:
                return ledgerBalancesAccountBalance.getUnsettleBuy().negate().add(ledgerBalancesAccountBalance.getUnsettleSell());
            case PENDING_OTHER:
            case TREASURY:
                return ledgerBalancesAccountBalance.getUnsettleBuy().subtract(ledgerBalancesAccountBalance.getUnsettleSell());
            case TRADER:
                return ledgerBalancesAccountBalance.getAvailableBalance().add(ledgerBalancesAccountBalance.getBrokerage())
                        .add(ledgerBalancesAccountBalance.getExchangeAvailableBalance()).add(ledgerBalancesAccountBalance.getCredit().negate())
                        .add(ledgerBalancesAccountBalance.getUnprocessedDeposit()).add(ledgerBalancesAccountBalance.getUnprocessedWithdrawal())
                        .add(ledgerBalancesAccountBalance.getHold()).add(ledgerBalancesAccountBalance.getSuspense());
            case PENDING_TRADER:
                return ledgerBalancesAccountBalance.getAvailableBalance().add(ledgerBalancesAccountBalance.getHold())
                        .add(ledgerBalancesAccountBalance.getSuspense()).add(ledgerBalancesAccountBalance.getOrder())
                        .add(ledgerBalancesAccountBalance.getExchangeAvailableBalance()).add(ledgerBalancesAccountBalance.getLeverageAvailableBalance())
                        .add(ledgerBalancesAccountBalance.getCredit()).add(ledgerBalancesAccountBalance.getPendingWithdrawal())
                        .add(ledgerBalancesAccountBalance.getUnsettleBuy()).subtract(ledgerBalancesAccountBalance.getUnsettleSell());
            case INHOUSE:
                return ledgerBalancesAccountBalance.getAvailableBalance().add(ledgerBalancesAccountBalance.getBrokerage())
                        .add(ledgerBalancesAccountBalance.getHold()).add(ledgerBalancesAccountBalance.getSuspense())
                        .add(ledgerBalancesAccountBalance.getOrder()).add(ledgerBalancesAccountBalance.getPendingWithdrawal().negate())
                        .add(ledgerBalancesAccountBalance.getExchangeAvailableBalance()).add(ledgerBalancesAccountBalance.getLeverageAvailableBalance())
                        .add(ledgerBalancesAccountBalance.getCollateral()).add(ledgerBalancesAccountBalance.getUnprocessedDeposit())
                        .add(ledgerBalancesAccountBalance.getUnprocessedWithdrawal()).add(ledgerBalancesAccountBalance.getUnconfirmed());
            case TRADE_AHEAD:
                return ledgerBalancesAccountBalance.getCredit().subtract(ledgerBalancesAccountBalance.getHold());
            default:
                return BigDecimal.ZERO;
        }
    }

    private <T extends Iterable> void publishToInflux(T payload) {
        payload.forEach(e ->
                dataFeedService.publishToInflux(e)
        );
    }

    private void completeStateForPlatformLedgerBalances(List<LedgerBalancesUser> userUuids) {
        Set<String> uuids = userUuids.stream().map(LedgerBalancesUser::getUserUuid).collect(Collectors.toSet());
        platformLedgerBalancesEntityService.updateCompletedStateForPlatformLedgerBalances(
                uuids, true
        );
        logger.info(
                "Status was set to completed for {} platform ledger balances",
                uuids)
        ;
    }
}
