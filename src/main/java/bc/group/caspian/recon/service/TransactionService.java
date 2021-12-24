package bc.group.caspian.recon.service;

import bc.group.caspian.recon.config.caspian.CaspianEndpointProperties;
import group.bc.caspian.connector.config.CaspianConnectionProperties;
import group.bc.caspian.connector.model.QueryParam;
import group.bc.caspian.connector.model.response.RestGetTransactionResponse;
import group.bc.caspian.connector.model.Transaction;
import group.bc.caspian.connector.service.CaspianRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TransactionService {

    @Autowired
    private CaspianRestService caspianRestService;

    @Autowired
    private CaspianEndpointProperties caspianEndpointProperties;

    @Autowired
    private CaspianConnectionProperties caspianConnectionProperties;

    private RestTemplate restTemplate = new RestTemplate();

    public List<Transaction> getTransactions(String fund, String startDay, String endDay, String organization, String... properties) {
        String getTransactionsEndpoint = caspianEndpointProperties.getGetTransaction();
        List<QueryParam> params = new ArrayList<>();
        params.add(new QueryParam("fund", fund));
        params.add(new QueryParam("startDay", startDay));
        params.add(new QueryParam("endDay", endDay));
        if (properties.length > 0) {
            for(String property: properties) {
                params.add(new QueryParam("property", property));
            }
        }

        ResponseEntity<RestGetTransactionResponse> response = caspianRestService.getRequest(getTransactionsEndpoint, params, organization, RestGetTransactionResponse.class);

        return Objects.requireNonNull(response.getBody()).getTransactions();
    }

    public List<Transaction> getTransactions(Map<String, String> paramsMap, String organization) {
        String getTransactionsEndpoint = caspianEndpointProperties.getGetTransaction();
        List<QueryParam> params = new ArrayList<>();

        paramsMap.forEach( (k,v) -> params.add(new QueryParam(k,v)));

        ResponseEntity<RestGetTransactionResponse> response = caspianRestService.getRequest(getTransactionsEndpoint,params, organization, RestGetTransactionResponse.class);

        return Objects.requireNonNull(response.getBody()).getTransactions();
    }

    public ResponseEntity<Map> getPmsHealth () {
        String url = caspianConnectionProperties.getConnectionUrl() + "/v1/" + caspianConnectionProperties.getPathName() + "/ping";
        return restTemplate.getForEntity(url, Map.class);
    }
}
