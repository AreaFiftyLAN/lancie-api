package ch.wisv.areafiftylan.security.token;

import ch.wisv.areafiftylan.model.User;

import javax.persistence.Entity;

/**
 * Created by Sille Kamoen on 6-5-16.
 */
@Entity
public class AuthenticationToken extends Token {

    // Default 5 days validity
    private static final int EXPIRATION = 60 * 24 * 5;

    public AuthenticationToken() {
        // JPA ONLY
    }

    public AuthenticationToken(User user) {
        super(user, EXPIRATION);
    }
}
