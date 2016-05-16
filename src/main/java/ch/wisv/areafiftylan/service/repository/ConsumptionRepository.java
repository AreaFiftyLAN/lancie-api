package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.ConsumptionMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by beer on 16-5-16.
 */
@Repository
public interface ConsumptionRepository extends JpaRepository<ConsumptionMap, Long> {
    Optional<ConsumptionMap> findByTicketId(Long ticketId);
}
