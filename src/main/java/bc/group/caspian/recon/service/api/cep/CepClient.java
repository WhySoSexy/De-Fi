package bc.group.caspian.recon.service.api.cep;

import bc.group.caspian.recon.api.cep.CepHedgeTradeRequest;
import bc.group.caspian.recon.api.cep.CepHedgeTradeResponse;
import bc.group.caspian.recon.config.cep.CepProperties;
import bc.group.caspian.recon.exception.CepClientRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
@Slf4j
public class CepClient {

    private final ObjectMapper objectMapper;
    private final CepProperties cepProperties;
    private final RestTemplate restTemplate;
    private final static Logger logger = LoggerFactory.getLogger(CepClient.class);

    public CepClient(CepProperties cepProperties, ObjectMapper objectMapper, RestTemplateBuilder restTemplateBuilder) {
        this.cepProperties = cepProperties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> new BufferingClientHttpRequestFactory(new OkHttp3ClientHttpRequestFactory()))
                .build();
    }

    private Mac createSigner() throws NoSuchAlgorithmException, InvalidKeyException {
        Mac signer = Mac.getInstance("HmacSHA512");
        signer.init(new SecretKeySpec(Base64.getDecoder().decode(cepProperties.getSecret().getBytes()), "HmacSHA512"));
        return signer;
    }

    public CepHedgeTradeResponse getCepTrades(CepHedgeTradeRequest request) {
        return postForObject(cepProperties.getEndpoint(), request, CepHedgeTradeResponse.class);
    }

    private <T> T postForObject(String endpoint, Object requestBody, Class<T> responseType) {
        try {
            Map<String, Object> requestBodyMap = objectMapper.convertValue(requestBody, Map.class);
            String nonce = String.valueOf(Instant.now().toEpochMilli());
            requestBodyMap.put("nonce", nonce);
            String requestBodyContent = objectMapper.writeValueAsString(requestBodyMap);
            logger.info("Requesting cep api with body content {} ", requestBodyContent);

            String restKey = cepProperties.getApiKey();
            String restSign = createSignature(endpoint, requestBodyContent);

            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("Rest-Key", restKey);
            headers.add("Rest-Sign", restSign);
            HttpEntity<String> httpEntity = new HttpEntity<>(requestBodyContent, headers);

            URI uri = UriComponentsBuilder.fromHttpUrl(cepProperties.getHost())
                    .path(endpoint)
                    .build()
                    .toUri();

            return restTemplate.postForObject(uri, httpEntity, responseType);
        } catch (Exception e) {
            throw new CepClientRequestException("failed to send cep request", e, endpoint, requestBody);
        }
    }

    private String createSignature(String endpoint, String requestBody) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] signPayload = (endpoint + '\0' + requestBody).getBytes();
        Mac signer = createSigner();
        return Base64.getEncoder().encodeToString(signer.doFinal(signPayload));

    }
}
