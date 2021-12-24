package bc.group.caspian.recon.service;

import bc.group.caspian.recon.domain.mysql.PlatformLedgerBalanceEntity;
import bc.group.caspian.recon.repository.PlatformLedgerBalanceRepository;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlatformLedgerBalancesEntityService {

    private final PlatformLedgerBalanceRepository platformLedgerBalanceRepository;

    public PlatformLedgerBalancesEntityService(
            PlatformLedgerBalanceRepository platformLedgerBalanceRepository
    ) {
        this.platformLedgerBalanceRepository = platformLedgerBalanceRepository;
    }

    public void updateCompletedStateForPlatformLedgerBalances(Set<String> uuids, boolean isCompleted) {
        List<PlatformLedgerBalanceEntity> entities = uuids.stream()
                .map(uuid -> {
                    PlatformLedgerBalanceEntity entity = updateOrCreateEntity(uuid);
                    entity.setCompleted(isCompleted);
                    return entity;
                })
                .collect(Collectors.toList());

        platformLedgerBalanceRepository.saveAll(entities);
    }

    protected PlatformLedgerBalanceEntity updateOrCreateEntity(String uuid) {
        PlatformLedgerBalanceEntity entity = platformLedgerBalanceRepository.findFirstByUserId(uuid);
        if (entity == null) {
            entity = PlatformLedgerBalanceEntity.builder()
                    .userId(uuid)
                    .build();
        }
        return entity;
    }

    public List<String> findUncompletedUserIds() {
       return platformLedgerBalanceRepository.findUserIdByCompletedFalseOrderByUserIdAsc();
    }
}
