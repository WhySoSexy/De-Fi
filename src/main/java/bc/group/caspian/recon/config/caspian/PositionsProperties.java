package bc.group.caspian.recon.config.caspian;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties("positions")
public class PositionsProperties {

    @Getter
    private List<Property> supported;

    @Data
    public static class Property {
        private String fund;
        private String portfolio;
        private String strategy;
        private List<String> custodian;
    }
}
