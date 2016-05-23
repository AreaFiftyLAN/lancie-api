package ch.wisv.areafiftylan.model.util;

import lombok.Getter;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import javax.persistence.Entity;

/**
 * Created by beer on 20-5-16.
 */
@Entity
public class Consumption {
    @GeneratedValue
    @Getter
    @Id
    Long id;

    @Getter
    String name;

    public Consumption() {
        // JPA Only
    }

    public Consumption(String name) {
        this.name = name;
    }
}
