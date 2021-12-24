package bc.group.caspian.recon.scheduler;

import bc.group.caspian.recon.job.*;
import bc.group.caspian.recon.service.config.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static bc.group.caspian.recon.job.OtcTradeBookingJob.*;

@Profile("!test")
@Service
public class MainScheduler {

    @Value("${recon-feeds.job-enabled}")
    private boolean reconFeedsJobEnabled;

    @Value("${recon-feeds-push-retry.job-enabled}")
    private boolean pushRetryJobEnabled;

    @Value("${positions.job-enabled}")
    private boolean positionsJobEnabled;

    private CepHedgeTradeRetryJob cepHedgeTradeRetryJob;
    private CepHedgeTradeJob cepHedgeTradeJob;
    private OtcTradeBookingJob otcTradeBookingJob;
    private PlatformCoinTransactionJob platformCoinTransactionJob;
    private PlatformHedgeTradeJob platformHedgeTradeJob;
    private PlatformRfqTradeJob platformRfqTradeJob;
    private PlatformLedgerBalancesJob platformLedgerBalancesJob;
    private PlatformLedgerBalancesResetJob platformLedgerBalancesResetJob;
    private PmsReconFeedsJob pmsReconFeedsJob;
    private PmsReconPushRetryJob pmsReconPushRetryJob;
    private PositionsJob positionsJob;
    private CoinTransactionLedgerBalancesJob coinTransactionLedgerBalancesJob;

    private final CepHedgeTradeRetryProperties cepHedgeTradeRetryProperties;
    private final CepHedgeTradeProperties cepHedgeTradeProperties;
    private final PlatformOtcTradeProperties otcTradeProperties;
    private final PlatformCoinTransactionProperties platformCoinTransactionProperties;
    private final PlatformHedgeTradeProperties platformHedgeTradeProperties;
    private final PlatformRfqTradeProperties platformRfqTradeProperties;
    private final PlatformLedgerBalancesProperties platformLedgerBalancesProperties;
    private final CoinTransactionLedgerBalancesProperties coinTransactionLedgerBalancesProperties;
    public MainScheduler(CepHedgeTradeRetryJob cepHedgeTradeRetryJob,
                         CepHedgeTradeJob cepHedgeTradeJob,
                         OtcTradeBookingJob otcTradeBookingJob,
                         PlatformCoinTransactionJob platformCoinTransactionJob,
                         PlatformHedgeTradeJob platformHedgeTradeJob,
                         PlatformRfqTradeJob platformRfqTradeJob,
                         PlatformLedgerBalancesJob platformLedgerBalancesJob,
                         PlatformLedgerBalancesResetJob platformLedgerBalancesResetJob,
                         PmsReconFeedsJob pmsReconFeedsJob,
                         PmsReconPushRetryJob pmsReconPushRetryJob,
                         PositionsJob positionsJob,
                         CoinTransactionLedgerBalancesJob coinTransactionLedgerBalancesJob,
                         CepHedgeTradeRetryProperties cepHedgeTradeRetryProperties,
                         CepHedgeTradeProperties cepHedgeTradeProperties,
                         PlatformRfqTradeProperties platformRfqTradeProperties,
                         PlatformOtcTradeProperties otcTradeProperties,
                         PlatformCoinTransactionProperties platformCoinTransactionProperties,
                         PlatformHedgeTradeProperties platformHedgeTradeProperties,
                         PlatformLedgerBalancesProperties platformLedgerBalancesProperties,
                         CoinTransactionLedgerBalancesProperties coinTransactionLedgerBalancesProperties) {
        this.cepHedgeTradeRetryJob = cepHedgeTradeRetryJob;
        this.cepHedgeTradeJob = cepHedgeTradeJob;
        this.otcTradeBookingJob = otcTradeBookingJob;
        this.platformCoinTransactionJob = platformCoinTransactionJob;
        this.platformHedgeTradeJob = platformHedgeTradeJob;
        this.platformRfqTradeJob = platformRfqTradeJob;
        this.platformLedgerBalancesJob = platformLedgerBalancesJob;
        this.platformLedgerBalancesResetJob = platformLedgerBalancesResetJob;
        this.pmsReconFeedsJob = pmsReconFeedsJob;
        this.pmsReconPushRetryJob = pmsReconPushRetryJob;
        this.positionsJob = positionsJob;
        this.coinTransactionLedgerBalancesJob = coinTransactionLedgerBalancesJob;
        this.cepHedgeTradeRetryProperties = cepHedgeTradeRetryProperties;
        this.cepHedgeTradeProperties = cepHedgeTradeProperties;
        this.otcTradeProperties = otcTradeProperties;
        this.platformCoinTransactionProperties = platformCoinTransactionProperties;
        this.platformHedgeTradeProperties = platformHedgeTradeProperties;
        this.platformRfqTradeProperties = platformRfqTradeProperties;
        this.platformLedgerBalancesProperties = platformLedgerBalancesProperties;
        this.coinTransactionLedgerBalancesProperties = coinTransactionLedgerBalancesProperties;
    }

    @Scheduled(fixedDelayString = "${cep-hedge-feeds-retry.fix-delay-string:60000}")
    private void initHedgeRetryJob() {
        if (cepHedgeTradeRetryProperties.getJobEnabled()) {
            cepHedgeTradeRetryJob.runHedgeRetryJob();
        }
    }

    @Scheduled(fixedDelayString = "${cep-hedge-feeds.fix-delay-string:60000}")
    private void initHedgeJob() {
        if (cepHedgeTradeProperties.getJobEnabled()) {
            cepHedgeTradeJob.runHedgeJob();
        }
    }

    @Scheduled(fixedDelayString = "${otc-booking-feeds.fix-delay-string:60000}")
    private void initOtcTradeBookingJob() {
        if (otcTradeProperties.getJobEnabled()) {
            otcTradeBookingJob.runOtcTradeBookingsJob(PENDING);
            otcTradeBookingJob.runOtcTradeBookingsJob(VERIFIED);
            otcTradeBookingJob.runOtcTradeBookingsJob(PRE_VERIFICATION);
            otcTradeBookingJob.runOtcTradeBookingsJob(POST_VERIFICATION);
        }
    }

    @Scheduled(fixedDelayString = "${platform-coin-txn.fix-delay-string:60000}")
    private void initPlatformCoinTransactionJob() {
        if (platformCoinTransactionProperties.getJobEnabled()) {
            platformCoinTransactionJob.runPlatformTransactionJob();
        }
    }

    @Scheduled(fixedDelayString = "${platform-hedge-feeds.fix-delay-string:60000}")
    private void initPlatformJob() {
        if (platformHedgeTradeProperties.getJobEnabled()) {
            platformHedgeTradeJob.runPlatformHedgeTradeJob();
            platformHedgeTradeJob.runPlatformHedgeTradeRetryJob();
        }
    }

    @Scheduled(fixedDelayString = "${ledger-balances.fix-delay-string:6000}", zone = "${spring.timezone}")
    private void initAccountBalancesJob() {
        if (platformLedgerBalancesProperties.getJobEnabled()) {
            platformLedgerBalancesJob.runAccountBalancesJob();
        }
    }

    @Scheduled(cron = "${ledger-balances.cron:0 0 0,9,17 * * *}", zone = "${spring.timezone}")
    private void initAccountBalancesResetJob() {
        if (platformLedgerBalancesProperties.getJobEnabled()) {
            platformLedgerBalancesResetJob.runAccountBalancesJob();
        }
    }

    @Scheduled(fixedDelayString = "${recon-feeds.fix-delay-string:300000}")
    private void initReconFeedJob() {
        if (reconFeedsJobEnabled) {
            pmsReconFeedsJob.runReconFeedsJob();
        }
    }

    @Scheduled(fixedDelayString = "${recon-feeds-push-retry.fix-delay-string:60000}")
    private void initPushRetryJob() {
        if (pushRetryJobEnabled) {
            pmsReconPushRetryJob.runPushRetryJob();
        }
    }

    @Scheduled(fixedDelayString = "${platform-rfq-trades.fix-delay-string:60000}")
    private void initTradesJob() {
        if (platformRfqTradeProperties.getJobEnabled()) {
            platformRfqTradeJob.runTradesJob();
            platformRfqTradeJob.runTradesRetryJob();
        }
    }

    @Scheduled(cron = "${positions.cron}", zone = "${spring.timezone}")
    private void initPositionsJob() {
        if (positionsJobEnabled) {
            positionsJob.runPositionsJob();
        }
    }

    @Scheduled(cron = "${txn-ledger-balances.cron}", zone = "${spring.timezone}")
    private void initTxnGetLedgerBalanceJob() {
        if (coinTransactionLedgerBalancesProperties.getJobEnabled()) {
            coinTransactionLedgerBalancesJob.runTxnGetLedgerBalanceJob();
        }
    }
}
