package bc.group.caspian.recon.repository;

import bc.group.caspian.recon.domain.mysql.TransactionEntity;
import bc.group.caspian.recon.domain.mysql.TransactionEntityPK;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface TransactionRepository extends CrudRepository<TransactionEntity, TransactionEntityPK> {

    @Override
    List<TransactionEntity> findAll();

    @Override
    Optional<TransactionEntity> findById(TransactionEntityPK id);

    List<TransactionEntity> findByInserted(Boolean inserted);

   Optional<TransactionEntity> findTopByInsertedOrderByUpdateTimestampDesc(boolean inserted);

   Optional<TransactionEntity> findTopByInsertedOrderByUpdateTimestampAsc(boolean inserted);

}
