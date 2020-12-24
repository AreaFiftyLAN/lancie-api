/*
 * Copyright (c) 2018  W.I.S.V. 'Christiaan Huygens'
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.wisv.areafiftylan.security.authentication;

import ch.wisv.areafiftylan.exception.InvalidTokenException;
import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.exception.XAuthTokenNotFoundException;
import ch.wisv.areafiftylan.security.token.AuthenticationToken;
import ch.wisv.areafiftylan.security.token.PasswordResetToken;
import ch.wisv.areafiftylan.security.token.VerificationToken;
import ch.wisv.areafiftylan.security.token.repository.AuthenticationTokenRepository;
import ch.wisv.areafiftylan.security.token.repository.PasswordResetTokenRepository;
import ch.wisv.areafiftylan.security.token.repository.VerificationTokenRepository;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserService;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationTokenRepository authenticationTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final UserService userService;

    @Autowired
    public AuthenticationServiceImpl(AuthenticationTokenRepository authenticationTokenRepository,
                                     VerificationTokenRepository verificationTokenRepository,
                                     PasswordResetTokenRepository passwordResetTokenRepository,
                                     UserService userService) {
        this.authenticationTokenRepository = authenticationTokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userService = userService;
    }

    @Override
    public String createNewAuthToken(String email) {
        User user = userService.getUserByEmail(email);

        // Delete the old Tokens if there are more than 4.
        List<AuthenticationToken> existingTokens = authenticationTokenRepository.findByUserEmail(email);
        if (existingTokens.size() >= 4) {
            authenticationTokenRepository.delete(existingTokens.get(0));
        }

        return authenticationTokenRepository.saveAndFlush(new AuthenticationToken(user)).getToken();
    }


    @Override
    public void removeAuthToken(String xAuth) {
        if (Strings.isNullOrEmpty(xAuth)) {
            throw new IllegalArgumentException("No X-Auth-Token present");
        }

        AuthenticationToken token =
                authenticationTokenRepository.findByToken(xAuth).orElseThrow(XAuthTokenNotFoundException::new);

        token.revoke();
        authenticationTokenRepository.saveAndFlush(token);
    }

    @Override
    public void removeAuthTokenForUser(User user) {
        authenticationTokenRepository.deleteByUser(user);
    }

    @Override
    public void removeAllAuthTokens() {
        authenticationTokenRepository.deleteAll();
        log.info("Deleted all authentication tokens");
    }

    @Override
    public User verifyUserByToken(String token) {
        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        // Get the user associated with this token
        User user = verificationToken.getUser();
        if (!verificationToken.isValid()) {
            throw new InvalidTokenException();
        }

        userService.verify(user.getId());

        //Disable the token for future use.
        verificationToken.use();
        verificationTokenRepository.saveAndFlush(verificationToken);

        return user;
    }

    @Override
    public void resetPasswordByToken(String token, String password) {
        if (token == null || Strings.isNullOrEmpty(password)) {
            throw new IllegalArgumentException("Token and password fields should be set");
        }

        PasswordResetToken passwordResetToken =
                passwordResetTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        //Check validity of the token
        if (!passwordResetToken.isValid()) {
            throw new InvalidTokenException();
        }

        // Get the user associated to the token
        User user = passwordResetToken.getUser();
        userService.resetPassword(user.getId(), password);

        // Disable the token for future use.
        passwordResetToken.use();
        passwordResetTokenRepository.saveAndFlush(passwordResetToken);

        removeAuthTokenForUser(user);
    }
}
