package ch.wisv.areafiftylan.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by sille on 29-1-16.
 */
public class PasswordChangeDTO {

    @Getter
    @Setter
    @NotEmpty
    String oldPassword = "";
    @Getter
    @Setter
    @NotEmpty
    String newPassword = "";

}
