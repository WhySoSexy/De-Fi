package bc.group.caspian.recon.domain.mysql;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Table(name = "cep_hedge_trade")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CepHedgeTradeEntity implements Serializable {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "completed", nullable = false)
    private Boolean completed;

    @Column(name = "existing", columnDefinition = "BOOLEAN DEFAULT 1")
    private Boolean existing;
}
