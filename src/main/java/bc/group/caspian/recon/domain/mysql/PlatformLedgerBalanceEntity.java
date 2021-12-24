package bc.group.caspian.recon.domain.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.influxdb.annotation.Column;

import javax.persistence.*;

@Entity
@Data
@Builder
@Table(name = "platform_ledger_balance")
@NoArgsConstructor
@AllArgsConstructor
public class PlatformLedgerBalanceEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "completed")
    private Boolean completed;
}
