package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.util.Consumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by beer on 20-5-16.
 */
@Repository
public interface PossibleConsumptionsRepository extends JpaRepository<Consumption, String> {
}
