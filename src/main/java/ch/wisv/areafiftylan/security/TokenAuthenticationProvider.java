package ch.wisv.areafiftylan.security;

import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.security.token.AuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Created by Sille Kamoen on 6-5-16.
 */
@Component
public class TokenAuthenticationProvider implements AuthenticationProvider {

    public TokenAuthenticationProvider() {
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        AuthenticationToken tokenAuthentication = (AuthenticationToken) authentication;
        User user = tokenAuthentication.getUser();

        return (Authentication) tokenAuthentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AuthenticationToken.class.isAssignableFrom(authentication);
    }

}
