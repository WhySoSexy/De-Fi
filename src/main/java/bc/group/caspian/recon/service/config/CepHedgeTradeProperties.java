package bc.group.caspian.recon.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "cep-hedge-feeds")
@Component
@Data
public class CepHedgeTradeProperties {

    private Boolean jobEnabled;
    private String fixDelayString;
    private Long startId;
    private List<String> valid;
    private String invalid;
    private Long batchSize;
    private List<String> treasuryUsers;
    private List<String> siteGroups;
}
