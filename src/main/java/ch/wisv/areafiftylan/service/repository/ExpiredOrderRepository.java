package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.ExpiredOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by sille on 23-12-15.
 */

@Repository
public interface ExpiredOrderRepository extends JpaRepository<ExpiredOrder, Long> {
    Collection<ExpiredOrder> findAllBycreatedByIgnoreCase(String username);

}
