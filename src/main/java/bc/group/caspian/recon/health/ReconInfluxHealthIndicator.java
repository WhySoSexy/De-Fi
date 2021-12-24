package bc.group.caspian.recon.health;

import lombok.Getter;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Pong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Profile("!test")
@Component
public class ReconInfluxHealthIndicator implements HealthIndicator {
    @Autowired
    @Getter
    private InfluxDB influxDB;

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();
        try {
            Pong pong = influxDB.ping();
            if (pong.isGood() && !pong.getVersion().equalsIgnoreCase("unknown"))
                healthBuilder.up();
            else
                healthBuilder.down();
        } catch (RestClientException ex) {
            healthBuilder.down();
        }

        return healthBuilder.build();
    }
}
