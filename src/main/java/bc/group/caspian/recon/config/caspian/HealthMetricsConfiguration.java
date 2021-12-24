package bc.group.caspian.recon.config.caspian;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
class HealthMetricsConfiguration {

    // This should be a field so it doesn't get garbage collected
    public HealthMetricsConfiguration(Map<String, HealthIndicator> healthIndicators,
                                      MeterRegistry registry) {

        registry.gauge("health", Tags.of("details", "caspianApi"), healthIndicators.get("caspianApiHealthIndicator"), health -> {
            Status status = health.health().getStatus();
            switch (status.getCode()) {
                case "UP":
                    return 3;
                case "OUT_OF_SERVICE":
                    return 2;
                case "DOWN":
                    return 1;
                case "UNKNOWN":
                default:
                    return 0;
            }
        });

        registry.gauge("health", Tags.of("details", "cepApi"), healthIndicators.get("cepApiHealthIndicator"), health -> {
            Status status = health.health().getStatus();
            switch (status.getCode()) {
                case "UP":
                    return 3;
                case "OUT_OF_SERVICE":
                    return 2;
                case "DOWN":
                    return 1;
                case "UNKNOWN":
                default:
                    return 0;
            }
        });

        registry.gauge("health", Tags.of("details", "platformApi"), healthIndicators.get("platformApiHealthIndicator"), health -> {
            Status status = health.health().getStatus();
            switch (status.getCode()) {
                case "UP":
                    return 3;
                case "OUT_OF_SERVICE":
                    return 2;
                case "DOWN":
                    return 1;
                case "UNKNOWN":
                default:
                    return 0;
            }
        });

        registry.gauge("health", Tags.of("details", "reconInflux"), healthIndicators.get("reconInfluxHealthIndicator"), health -> {
            Status status = health.health().getStatus();
            switch (status.getCode()) {
                case "UP":
                    return 3;
                case "OUT_OF_SERVICE":
                    return 2;
                case "DOWN":
                    return 1;
                case "UNKNOWN":
                default:
                    return 0;
            }
        });
    }
}