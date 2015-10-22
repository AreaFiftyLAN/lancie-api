package ch.wisv.areafiftylan.security;

import ch.wisv.areafiftylan.model.User;

import javax.persistence.Entity;

@Entity
public class PasswordResetToken extends Token {


    public PasswordResetToken() {
    }

    public PasswordResetToken(String token, User user) {
        super(token, user);
    }

}