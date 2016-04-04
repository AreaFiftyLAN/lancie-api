package ch.wisv.areafiftylan.security.token;

import ch.wisv.areafiftylan.model.User;

import javax.persistence.Entity;

@Entity
public class VerificationToken extends Token {

    public VerificationToken() {
    }

    public VerificationToken(User user) {
        super(user);
    }
}