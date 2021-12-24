package bc.group.caspian.recon.health;

import bc.group.caspian.recon.config.cep.CepProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

@Profile("!test")
@Component
public class CepApiHealthIndicator implements HealthIndicator {

    private final CepProperties cepProperties;
    private final RestTemplate restTemplate;

    public CepApiHealthIndicator (CepProperties cepProperties, RestTemplateBuilder restTemplateBuilder){
        this.cepProperties = cepProperties;
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> new BufferingClientHttpRequestFactory(new OkHttp3ClientHttpRequestFactory()))
                .setConnectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();
        try {
            ResponseEntity<Map> res = getCepHealth();
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

    private ResponseEntity<Map> getCepHealth () {
        String url = cepProperties.getHost() + "version";
        return restTemplate.getForEntity(url, Map.class);
    }
}
