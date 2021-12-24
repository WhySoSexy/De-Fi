package bc.group.caspian.recon.repository;

import bc.group.caspian.recon.domain.mysql.PlatformHedgeTradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PlatformHedgeTradeRepository extends JpaRepository<PlatformHedgeTradeEntity, Long> {

    @Query("SELECT platformHedgeTradeEntity.tradeId " +
            "FROM PlatformHedgeTradeEntity platformHedgeTradeEntity " +
            "WHERE platformHedgeTradeEntity.completed = false Order By platformHedgeTradeEntity.tradeId desc")
    List<Long> findTradeIdByCompletedFalseOrderByTradeIdDesc();

    @Modifying
    @Transactional
    @Query("update PlatformHedgeTradeEntity platformHedgeTradeEntity set platformHedgeTradeEntity.completed = :completed where platformHedgeTradeEntity.tradeId = :tradeId")
    void updateCompletedInPlatformHedgeTradeId(@Param("tradeId") Long tradeId, @Param("completed") boolean completed);

    PlatformHedgeTradeEntity findPlatformHedgeTradeEntityByTradeId(Long id);
}
