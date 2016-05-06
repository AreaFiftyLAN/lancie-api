package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.security.token.AuthenticationToken;
import ch.wisv.areafiftylan.service.repository.token.AuthenticationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created by Sille Kamoen on 6-5-16.
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    AuthenticationTokenRepository authenticationTokenRepository;

    @Autowired
    UserService userService;

    @Override
    public String createNewAuthToken(String username, String password) {
        User user = userService.getUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));

        if (correctCredentials(user, password)) {
            // Delete the old Token
            authenticationTokenRepository.findByUserUsername(username)
                    .ifPresent(t -> authenticationTokenRepository.delete(t));

            return authenticationTokenRepository.save(new AuthenticationToken(user, 60 * 24 * 2)).getToken();
        }

        throw new AuthenticationCredentialsNotFoundException("Incorrect credentials");
    }

    private boolean correctCredentials(User user, String password) {
        return new BCryptPasswordEncoder().matches(password, user.getPassword());
    }
}
