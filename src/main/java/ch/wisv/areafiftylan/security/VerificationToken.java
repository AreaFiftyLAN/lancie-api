package ch.wisv.areafiftylan.security;

import ch.wisv.areafiftylan.model.User;

import javax.persistence.Entity;

@Entity
public class VerificationToken extends Token {

    public VerificationToken() {
    }

    public VerificationToken(String token, User user) {
        super(token, user);
    }
}