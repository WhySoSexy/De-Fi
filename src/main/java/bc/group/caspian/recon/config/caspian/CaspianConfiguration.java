package bc.group.caspian.recon.config.caspian;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import group.bc.caspian.connector.config.CaspianConnectionProperties;
import group.bc.caspian.connector.service.CaspianRestService;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.influx.InfluxDbOkHttpClientBuilderProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CaspianConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "caspian")
    public CaspianConnectionProperties caspianConnectionProperties() {
        return new CaspianConnectionProperties();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public CaspianRestService caspianRestService(CaspianConnectionProperties caspianConnectionProperties, RestTemplateBuilder restTemplateBuilder) throws InvalidKeyException, NoSuchAlgorithmException {
        return new CaspianRestService(caspianConnectionProperties, restTemplateBuilder.build());
    }

    @Bean
    public InfluxDbOkHttpClientBuilderProvider influxDbOkHttpClientBuilderProvider() throws NoSuchAlgorithmException, KeyManagementException {
        // FIXME: resume the certificate checking after fixing the domain or certificate on UAT
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        // Create an ssl socket factory with our all-trusting manager
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        return () -> builder;
    }

    @Bean
    public Map<String, String> caspianReconQueryParameters() {
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("property", "inputTimestamp,updateTimestamp,instrument,currency,additionalData,externalCode,netAmount,rate,status,owner,orderFeedId,side");
        queryParam.put("transactionType", "spotFx, cash");
        queryParam.put("portfolio",
                "Fund OSLHK:RFQ,Fund OSLHK:OTC,Fund OSLHK:OTCFLOW," +
                        "Fund OSLSG:RFQ,Fund OSLSG:OTCFLOW,Fund OSLSG:ARB,Fund OSLSG:PROP," +
                        "Fund OSLDS:OTC,Fund OSLDS:RFQ," +
                        "Fund OSLAM:OTC,Fund OSLAM:RFQ," +
                        "Fund Zodia:OTC,Fund Zodia:RFQ");
        queryParam.put("custodian", "OSLBLOCKS,Jump,B2C2,Cumberland,OSLDSTRDSPT,OSLSGSDS,OSLAMTRDSPT,OSLAMRFQETH," +
                "OSLAMRFQBTC,OSLAMRFQUSDT,OSLDSRFQETH,OSLDSRFQBTC,OSLDSRFQUSDT,OSLETHDEALER,OSLBTCDEALER,OSLUSDTDEALER,ZODIATRDSPT," +
                "ZODIARFQBTC,ZODIARFQETH,ZODIARFQUSDT");
        queryParam.put("tradeType", "OTC Trade, RFQ Trade, OTC, RFQ");
        queryParam.put("includeVoid", "true");
        return queryParam;
    }
}
