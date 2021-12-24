package bc.group.caspian.recon.repository;

import bc.group.caspian.recon.domain.mysql.CepHedgeTradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface CepHedgeTradeRepository extends JpaRepository<CepHedgeTradeEntity, Long> {

    Optional<CepHedgeTradeEntity> findTopByOrderByIdDesc();

    @Query("SELECT cepHedgeTradeEntity.id " +
            "FROM CepHedgeTradeEntity cepHedgeTradeEntity " +
            "WHERE (cepHedgeTradeEntity.completed = false and cepHedgeTradeEntity.existing = true) " +
            "Order By cepHedgeTradeEntity.id desc")
    List<Long> findIdByCompletedFalseOrderByIdDesc();

    @Modifying
    @Transactional
    @Query("update CepHedgeTradeEntity cepHedgeTradeEntity set cepHedgeTradeEntity.completed = :completed where cepHedgeTradeEntity.id = :Id")
    void updateCompletedInCepId(@Param("Id") Long cepid, @Param("completed") boolean completed);

    @Modifying
    @Transactional
    @Query("update CepHedgeTradeEntity cepHedgeTradeEntity set cepHedgeTradeEntity.existing = :existing where cepHedgeTradeEntity.id = :id")
    void updateExistingInCepId(@Param("id") Long cepId, @Param("existing") boolean existing);
}
