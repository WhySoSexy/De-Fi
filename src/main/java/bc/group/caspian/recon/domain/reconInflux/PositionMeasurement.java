package bc.group.caspian.recon.domain.reconInflux;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name="position")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionMeasurement {
     @Column(name = "fund")
     private String fund;

     @Column(name = "portfolio")
     private String portfolio;

     @Column(name = "instrument")
     private String instrument;

     @Column(name = "custodian")
     private String custodian;

     @Column(name = "strategy")
     private String strategy;

     @Column(name = "position")
     private String position;
}
