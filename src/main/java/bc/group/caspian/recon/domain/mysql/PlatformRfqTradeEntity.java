package bc.group.caspian.recon.domain.mysql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.influxdb.annotation.Column;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "platform_rfq_trade")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformRfqTradeEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_id")
    private String tradeId;

    @Column(name = "inserted")
    private Boolean completed;

    @Column(name = "date_created")
    private Timestamp dateCreated;

    @Column(name = "last_updated")
    private Timestamp lastUpdated;
}
