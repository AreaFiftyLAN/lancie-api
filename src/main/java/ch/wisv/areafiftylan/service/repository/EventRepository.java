package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by Sille Kamoen on 27-3-16.
 */
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findById(Long eventId);

}
