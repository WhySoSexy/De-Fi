package bc.group.caspian.recon.repository;

import bc.group.caspian.recon.domain.mysql.OtcTradeBookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OtcTradeBookingRepository extends JpaRepository<OtcTradeBookingEntity, Long> {

    OtcTradeBookingEntity findOtcTradeBookingEntityByTradeIdAndOtcBookingType(Long tradeId, String type);
}

