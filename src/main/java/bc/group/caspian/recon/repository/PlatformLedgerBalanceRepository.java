package bc.group.caspian.recon.repository;

import bc.group.caspian.recon.domain.mysql.PlatformLedgerBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatformLedgerBalanceRepository extends JpaRepository<PlatformLedgerBalanceEntity, Long> {
    PlatformLedgerBalanceEntity findFirstByUserId(String id);

    @Query("SELECT t.userId " +
            "FROM PlatformLedgerBalanceEntity t " +
            "WHERE t.completed = false Order By t.userId asc")
    List<String> findUserIdByCompletedFalseOrderByUserIdAsc();
}
