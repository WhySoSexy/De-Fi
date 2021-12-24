package bc.group.caspian.recon.health;

import bc.group.caspian.recon.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Profile("!test")
@Component
public class CaspianApiHealthIndicator implements HealthIndicator {
    @Autowired
    TransactionService transactionService;
    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();
        try {
            ResponseEntity<Map> res = transactionService.getPmsHealth();
            if (res.getStatusCode().is2xxSuccessful()) {
                healthBuilder.up();
            } else {
                healthBuilder.outOfService();
            }
        } catch (Exception ex) {
            healthBuilder.down();
        }
        return healthBuilder.build();
    }
}
