package ch.wisv.areafiftylan.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by Sille Kamoen on 27-3-16.
 */
public class EventDTO {

    @Getter
    @Setter
    @NotEmpty
    String name = "";

    @Getter
    @Setter
    @NotNull
    Integer teamSize;

    @Getter
    @Setter
    @NotNull
    Integer teamLimit;
}
