package ch.wisv.areafiftylan.unit;

import ch.wisv.areafiftylan.exception.SeatNotFoundException;
import ch.wisv.areafiftylan.seats.model.SeatGroupDTO;
import ch.wisv.areafiftylan.seats.service.SeatService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

        thrown.expect(SeatNotFoundException.class);
        thrown.expectMessage("SeatGroup " + seatGroupDTO.getSeatGroupName() + " not found!");

        seatService.removeSeats(seatGroupDTO);
    }

    @Test
    public void testRemoveSeatNegativeSeats() {
        SeatGroupDTO seatGroupDTO = new SeatGroupDTO();
        seatGroupDTO.setSeatGroupName(TEMP_SEATGROUP);
        seatGroupDTO.setNumberOfSeats(5);
        seatService.addSeats(seatGroupDTO);

        seatGroupDTO.setNumberOfSeats(-5);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Number of seats needs to be higher than 1");

        seatService.removeSeats(seatGroupDTO);
    }
}
