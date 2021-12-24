package bc.group.caspian.recon.service.config;

import bc.group.caspian.recon.domain.reconInflux.accountbalance.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ConfigurationProperties(prefix = "ledger-balances")
@Component
@Data
public class PlatformLedgerBalancesProperties {

    private Boolean jobEnabled;
    private String cron;
    private String batchSize;
    SupportedProperty supported;

    @Data
    public static class SupportedProperty {
        private List<String> currencies;

        private List<String> otherPendingUuids;
        private List<String> oslsgsPendingUuids;
        private List<String> inhouseUuids;
        private List<String> treasuryUuids;
        private List<String> traderUuids;
        private List<String> traderPendingUuids;
        private List<String> tradeAheadUuids;
        private List<CounterpartyProperty> counterpartyUuids;
        private List<OslsgsExcludedProperty> oslsgsExcludedUuids;

        @Data
        public static class CounterpartyProperty {
            private String source;
            private String counterparty;
        }

        @Data
        public static class OslsgsExcludedProperty {
            private String tradingspot;
            private String oslsgs;
        }

        public Set<String> getAccountUuids() {
            if (counterpartyUuids == null) counterpartyUuids = Collections.emptyList();
            List<String> allCounterpartyUuids = counterpartyUuids.stream()
                    .map(c -> Arrays.asList(c.source, c.counterparty))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            if (oslsgsExcludedUuids == null) oslsgsExcludedUuids = Collections.emptyList();
            List<String> allOslsgsExcludedUuids = oslsgsExcludedUuids.stream()
                    .map(c -> Arrays.asList(c.tradingspot, c.oslsgs))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

           return Stream.of(
                    otherPendingUuids, oslsgsPendingUuids,
                    inhouseUuids, treasuryUuids, traderUuids, traderPendingUuids,
                    allCounterpartyUuids, allOslsgsExcludedUuids, tradeAheadUuids
            )
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());
        }

        public List<String> getUuids(AccountBalanceType type) {
            switch (type) {
                case PENDING_OSLSGS:
                    return oslsgsPendingUuids;
                case PENDING_OTHER:
                    return otherPendingUuids;
                case TREASURY:
                    return treasuryUuids;
                case TRADER:
                    return traderUuids;
                case PENDING_TRADER:
                    return traderPendingUuids;
                case INHOUSE:
                    return inhouseUuids;
                case TRADE_AHEAD:
                    return tradeAheadUuids;
                default:
                    return Collections.emptyList();
            }
        }
    }
}



