package ch.wisv.areafiftylan.unit;

import ch.wisv.areafiftylan.exception.SeatNotFoundException;
import ch.wisv.areafiftylan.seats.model.SeatGroupDTO;
import ch.wisv.areafiftylan.seats.service.SeatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SeatServiceTest extends ServiceTest {

    @Autowired
    private SeatService seatService;

    private final String TEMP_SEATGROUP = "tempSeatGroup";

    @Test
    public void testRemoveSeatInvalidGroup() {
        SeatGroupDTO seatGroupDTO = new SeatGroupDTO();
        seatGroupDTO.setSeatGroupName(TEMP_SEATGROUP);
        seatGroupDTO.setNumberOfSeats(5);
        seatService.addSeats(seatGroupDTO);

        seatGroupDTO.setSeatGroupName(TEMP_SEATGROUP + "invalid");

        assertThrows(SeatNotFoundException.class, () -> seatService.removeSeats(seatGroupDTO));
    }

    @Test
    public void testRemoveSeatNegativeSeats() {
        SeatGroupDTO seatGroupDTO = new SeatGroupDTO();
        seatGroupDTO.setSeatGroupName(TEMP_SEATGROUP);
        seatGroupDTO.setNumberOfSeats(5);
        seatService.addSeats(seatGroupDTO);

        seatGroupDTO.setNumberOfSeats(-5);

        assertThrows(IllegalArgumentException.class, () -> seatService.removeSeats(seatGroupDTO));
    }
}
