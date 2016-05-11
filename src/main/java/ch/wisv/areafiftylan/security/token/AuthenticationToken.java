package ch.wisv.areafiftylan.security.token;

import ch.wisv.areafiftylan.model.User;

import javax.persistence.Entity;

/**
 * Created by Sille Kamoen on 6-5-16.
 */
@Entity
public class AuthenticationToken extends Token {

    public AuthenticationToken() {
        // JPA ONLY
    }

    public AuthenticationToken(User user, int expiration) {
        super(user, expiration);
    }
}
