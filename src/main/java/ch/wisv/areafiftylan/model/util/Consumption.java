package ch.wisv.areafiftylan.model.util;

import lombok.Getter;
import javax.persistence.Id;

import javax.persistence.Entity;

/**
 * Created by beer on 20-5-16.
 */
@Entity
public class Consumption {
    @Getter
    @Id
    String name;

    public Consumption(String name) {
        this.name = name;
    }
}
