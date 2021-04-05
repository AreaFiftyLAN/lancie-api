package ch.wisv.areafiftylan.utils.setup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SetupRepository extends JpaRepository<SetupLog, Long> {
}
