package bc.group.caspian.recon.repository;

import bc.group.caspian.recon.domain.mysql.SchedulerStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledStatusRepository extends JpaRepository<SchedulerStatusEntity, Long> {

    SchedulerStatusEntity findSchedulerStatusEntityByName(String name);
}
