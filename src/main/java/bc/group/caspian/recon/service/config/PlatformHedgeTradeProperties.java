package bc.group.caspian.recon.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "platform-hedge-feeds")
@Component
@Data
public class PlatformHedgeTradeProperties {

    private Boolean jobEnabled;
    private String batchSize;
    private Long startDate;
    private List<String> siteGroups;
}
