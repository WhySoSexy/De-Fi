package bc.group.caspian.recon.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "platform-coin-txn")
@Component
@Data
public class PlatformCoinTransactionProperties {

    private Boolean jobEnabled;
    private Long batchSize;
    private Long startDate;
}
