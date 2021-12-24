package bc.group.caspian.recon.config.caspian;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "caspian.endpoint")
public class CaspianEndpointProperties {

    private String createTransaction;
    private String getTransaction;
    private String getFXRate;
    private String getRisk;
    private String getPosition;
    private String getWorkbook;
    private String transactionStream;

}
