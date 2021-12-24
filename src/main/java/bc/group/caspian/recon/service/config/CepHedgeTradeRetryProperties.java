package bc.group.caspian.recon.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "cep-hedge-feeds-retry")
@Component
@Data
public class CepHedgeTradeRetryProperties {

    private Boolean jobEnabled;
    private String fixDelayString;
    private String batchSize;
    private String valid;
    private String invalid;
}
