package ch.wisv.areafiftylan.web.event.service;

import ch.wisv.areafiftylan.web.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

}
