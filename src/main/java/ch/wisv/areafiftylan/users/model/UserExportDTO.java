package ch.wisv.areafiftylan.users.model;

import ch.wisv.areafiftylan.seats.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserExportDTO {

    private Long userId;
    private String email;
    private String passwordHash;
    private String displayname;
    private List<String> seat;
}
