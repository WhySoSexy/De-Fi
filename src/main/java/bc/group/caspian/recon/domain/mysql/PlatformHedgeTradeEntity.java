package bc.group.caspian.recon.domain.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Table(name = "platform_hedge_trade")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformHedgeTradeEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_id")
    private Long tradeId;

    @Column(name = "completed", nullable = false)
    private Boolean completed;
}
