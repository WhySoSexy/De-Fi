package bc.group.caspian.recon.health;

import bc.group.caspian.recon.service.api.platform.PlatformClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!test")
@Slf4j
@Component
public class PlatformApiHealthIndicator implements HealthIndicator {

    private final PlatformClient client;

    public PlatformApiHealthIndicator(PlatformClient client) {
        this.client = client;
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();
        try {
            if (client.ping()) {
                healthBuilder.up();
            }else {
                healthBuilder.outOfService();
            }
        } catch (Exception e) {
            log.error("Platform health is down", e);
            healthBuilder.down();
        }
        return healthBuilder.build();
    }
}
