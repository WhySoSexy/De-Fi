package bc.group.caspian.recon.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "otc-booking-feeds")
@Component
@Data
public class PlatformOtcTradeProperties {

    private Boolean jobEnabled;
    private String batchSize;
    private Long startDate;
    private List<String> preVerification;
    private List<String> postVerification;
}
