package bc.group.caspian.recon.config.cep;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "cep")
@Component
@Data
public class CepProperties {

    private String host;
    private String apiKey;
    private String secret;
    private String endpoint;
}
