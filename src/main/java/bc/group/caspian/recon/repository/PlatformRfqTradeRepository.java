package bc.group.caspian.recon.repository;

import bc.group.caspian.recon.domain.mysql.PlatformRfqTradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatformRfqTradeRepository extends JpaRepository<PlatformRfqTradeEntity, Long> {

    PlatformRfqTradeEntity findFirstByOrderByTradeIdDesc();
    PlatformRfqTradeEntity findFirstByTradeId(String id);

    @Query("SELECT t.tradeId " +
            "FROM PlatformRfqTradeEntity t " +
            "WHERE t.completed = false Order By t.tradeId asc")
    List<String> findTradeIdByCompletedFalseOrderByTradeIdAsc();
}
