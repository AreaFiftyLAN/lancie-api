package ch.wisv.areafiftylan.security.token;

import ch.wisv.areafiftylan.users.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@NoArgsConstructor
public class SetupToken extends Token {
    @Getter
    private int year;

    public SetupToken(User user, int year) {
        super(user);
        this.year = year;
    }
}
