package bc.group.caspian.recon.service;

import bc.group.caspian.recon.config.caspian.CaspianEndpointProperties;
import group.bc.caspian.connector.config.CaspianConnectionProperties;
import group.bc.caspian.connector.model.Position;
import group.bc.caspian.connector.model.QueryParam;
import group.bc.caspian.connector.model.response.RestGetPositionResponse;
import group.bc.caspian.connector.service.CaspianRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PositionService {

    @Autowired
    private CaspianRestService caspianRestService;

    @Autowired
    CaspianEndpointProperties caspianEndpointProperties;

    @Autowired
    CaspianConnectionProperties caspianConnectionProperties;

    public List<Position> getPositions(String day, String... aggregations) {
        String getPositionEndpoint = caspianEndpointProperties.getGetPosition();

        List<QueryParam> params = new ArrayList<>();
        params.add(new QueryParam("day", day));
        if (aggregations.length > 0) {
            params.add(new QueryParam("aggregation", String.join(",", aggregations)));
        }

        ResponseEntity<RestGetPositionResponse> response = caspianRestService.getRequest(getPositionEndpoint, params, null, RestGetPositionResponse.class);
        return Objects.requireNonNull(response.getBody()).getPositions();
    }
}
