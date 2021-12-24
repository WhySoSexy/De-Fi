package bc.group.caspian.recon.service.api.platform;

import bc.group.caspian.recon.api.platform.coinTransaction.PlatformCoinTransactionRequest;
import bc.group.caspian.recon.api.platform.coinTransaction.PlatformCoinTransactionResponse;
import bc.group.caspian.recon.api.platform.coinTransactionLedgerBalances.CoinTransactionLedgerBalancesRequest;
import bc.group.caspian.recon.api.platform.coinTransactionLedgerBalances.CoinTransactionLedgerBalancesResponse;
import bc.group.caspian.recon.api.platform.hedgeTrades.PlatformHedgeTradeRequest;
import bc.group.caspian.recon.api.platform.hedgeTrades.PlatformHedgeTradeResponse;
import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesRequest;
import bc.group.caspian.recon.api.platform.ledgerBalances.LedgerBalancesResponse;
import bc.group.caspian.recon.api.platform.otcBookingTrades.OtcTradeBookingResponse;
import bc.group.caspian.recon.api.platform.otcBookingTrades.OtcTradeBookingWithTimeRequest;
import bc.group.caspian.recon.api.platform.rfqTrades.PlatformRfqTradeRequest;
import bc.group.caspian.recon.api.platform.rfqTrades.PlatformRfqTradeResponse;
import bc.group.caspian.recon.config.platform.PlatformProperties;
import bc.group.caspian.recon.exception.PlatformClientRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Component
public class PlatformClient {

    private final RestTemplate restTemplate;
    private final PlatformProperties platformProperties;
    private final ObjectMapper objectMapper;
    private final static Logger logger = LoggerFactory.getLogger(PlatformClient.class);

    public PlatformClient(RestTemplateBuilder restTemplateBuilder, PlatformProperties platformProperties, ObjectMapper objectMapper) {
        this.platformProperties = platformProperties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> new BufferingClientHttpRequestFactory(new OkHttp3ClientHttpRequestFactory()))
                .setConnectTimeout(Duration.ofSeconds(120))
                .setReadTimeout(Duration.ofSeconds(120))
                .build();
    }

    public OtcTradeBookingResponse getOtcTrades(OtcTradeBookingWithTimeRequest request) {
        return postForObject(request, OtcTradeBookingResponse.class, PlatformEndpoint.OTC);
    }

    public PlatformHedgeTradeResponse getHedgeTrades(PlatformHedgeTradeRequest request) {
        return postForObject(request, PlatformHedgeTradeResponse.class, PlatformEndpoint.HEDGE);
    }

    public LedgerBalancesResponse getLedgerBalances(LedgerBalancesRequest request) {
        return postForObject(request, LedgerBalancesResponse.class, PlatformEndpoint.LEDGER_BALANCES);
    }

    public PlatformRfqTradeResponse getRfqTrade(PlatformRfqTradeRequest request) {
        return postForObject(request, PlatformRfqTradeResponse.class, PlatformEndpoint.RFQ_TRADES);
    }

    public PlatformCoinTransactionResponse getCoinTransactions(PlatformCoinTransactionRequest request) {
        return postForObject(request, PlatformCoinTransactionResponse.class, PlatformEndpoint.COIN_TRANSACTION);
    }

    public CoinTransactionLedgerBalancesResponse getTxnLedgerBalances(CoinTransactionLedgerBalancesRequest request) {
        return postForObject(request, CoinTransactionLedgerBalancesResponse.class, PlatformEndpoint.TXN_LEDGER_BALANCES);
    }

    private <T> T postForObject(Object requestBody, Class<T> responseType, PlatformEndpoint type) {
        try {
            Map<String, Object> requestBodyMap = objectMapper.convertValue(requestBody, Map.class);
            String nonce = String.valueOf(Instant.now().toEpochMilli() * 1000L);
            requestBodyMap.put("nonce", nonce);

            String requestBodyContent = objectMapper.writeValueAsString(requestBodyMap);
            logger.info(requestBodyContent);

            String restKey = platformProperties.getApiKey();
            String host = platformProperties.getHost();
            String restSign = createSignature(getEndpoint(type), requestBodyContent);

            HttpEntity<String> httpEntity = new HttpEntity<>(requestBodyContent, getHeaders(restKey, restSign));

            URI uri = UriComponentsBuilder.fromHttpUrl(host)
                    .path(getEndpoint(type))
                    .build()
                    .toUri();
            return restTemplate.postForObject(uri, httpEntity, responseType);
        } catch (Exception e) {
            throw new PlatformClientRequestException("Failed to send platform request", e, getEndpoint(type), requestBody);
        }
    }

    private MultiValueMap<String, String> getHeaders(String restKey, String restSign) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Rest-Key", restKey);
        headers.add("Rest-Sign", restSign);
        headers.add("Site-Enum", "_OPS_COM");
        return headers;
    }

    private String getEndpoint(PlatformEndpoint type) {
        switch (type) {
            case OTC:
                return platformProperties.getOtcEndpoint();
            case HEDGE:
                return platformProperties.getHedgeEndpoint();
            case LEDGER_BALANCES:
                return platformProperties.getLedgerBalancesEndpoint();
            case RFQ_TRADES:
                return platformProperties.getRfqTradeEndpoint();
            case COIN_TRANSACTION:
                return platformProperties.getCoinTxnEndpoint();
            case TXN_LEDGER_BALANCES:
                return platformProperties.getCoinTxnLedgerBalancesEndpoint();
        }
        return "";
    }

    public Boolean ping() {
        String url = platformProperties.getHost();
        String path = platformProperties.getHealth();
        String queryUrl = url + "/" + path;
        Map version;
        try {
            version = restTemplate.getForObject(queryUrl, Map.class);
            return version.get(platformProperties.getHealth()) != null;
        } catch (RestClientException e) {
            return false;
        }
    }

    private Mac createSigner() throws NoSuchAlgorithmException, InvalidKeyException {
        Mac signer = Mac.getInstance("HmacSHA512");
        signer.init(new SecretKeySpec(Base64.getDecoder().decode(platformProperties.getSecret().getBytes()), "HmacSHA512"));
        return signer;
    }

    private String createSignature(String endpoint, String requestBody) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] signPayload = (endpoint + '\0' + requestBody).getBytes();
        Mac signer = createSigner();
        return Base64.getEncoder().encodeToString(signer.doFinal(signPayload));
    }
}