package ch.wisv.areafiftylan.security.token;

import ch.wisv.areafiftylan.model.User;

import javax.persistence.Entity;

@Entity
public class VerificationToken extends Token {
    private static final int EXPIRATION = 3 * 60 * 24; //Three days

    public VerificationToken() {
    }

    public VerificationToken(User user) {
        super(user, EXPIRATION);
    }
}