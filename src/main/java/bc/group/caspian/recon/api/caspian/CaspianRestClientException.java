package bc.group.caspian.recon.api.caspian;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaspianRestClientException {

    private String error;
    public String getError() {
        return error;
    }
}

