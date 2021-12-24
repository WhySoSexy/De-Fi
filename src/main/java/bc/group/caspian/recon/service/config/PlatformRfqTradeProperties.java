package bc.group.caspian.recon.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "platform-rfq-trades")
@Component
@Data
public class PlatformRfqTradeProperties {
    private Boolean jobEnabled;
    private Long batchSize;
    private String startDate;
    private List<String> siteGroups;
}
