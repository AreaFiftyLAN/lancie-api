package ch.wisv.areafiftylan.users.model;

import ch.wisv.areafiftylan.seats.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserExportDTO {

    private String email;
    private String password_hash;
    private Long userId;
    private List<Seat> seat;
}
