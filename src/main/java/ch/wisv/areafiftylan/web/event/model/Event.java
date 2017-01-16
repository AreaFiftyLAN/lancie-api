package ch.wisv.areafiftylan.web.event.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Event {

    @Getter
    @Id
    @GeneratedValue
    Long id;

    @Getter
    @Setter
    String name;

    @Getter
    @Setter
    LocalDateTime startTime;

    @Getter
    @Setter
    LocalDateTime endTime;
}
