package bc.group.caspian.recon.domain.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "scheduler_status")
public class SchedulerStatusEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "offset", nullable = false)
    private Long offset;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "from_ts", columnDefinition = "TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP")
    private Timestamp fromTs;

    @Column(name = "to_ts", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp toTs;
}
