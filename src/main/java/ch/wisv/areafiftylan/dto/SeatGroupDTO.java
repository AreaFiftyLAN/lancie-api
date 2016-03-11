package ch.wisv.areafiftylan.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by sille on 10-1-16.
 */
public class SeatGroupDTO {

    @NotEmpty
    @Getter
    @Setter
    String seatGroupName = "";

    @NotNull
    @Getter
    @Setter
    Integer numberOfSeats;


}
