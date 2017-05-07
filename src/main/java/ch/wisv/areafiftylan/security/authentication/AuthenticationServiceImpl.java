/*
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
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
import ch.wisv.areafiftylan.exception.XAuthTokenNotFoundException;
import ch.wisv.areafiftylan.security.token.AuthenticationToken;
import ch.wisv.areafiftylan.security.token.repository.AuthenticationTokenRepository;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserService;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationTokenRepository authenticationTokenRepository;

    private final UserService userService;

    @Autowired
    public AuthenticationServiceImpl(AuthenticationTokenRepository authenticationTokenRepository,
                                     UserService userService) {
        this.authenticationTokenRepository = authenticationTokenRepository;
        this.userService = userService;
    }

    @Override
    public String createNewAuthToken(String email) {
        User user = userService.getUserByEmail(email);

        // Delete the old Token
        authenticationTokenRepository.findByUserEmail(email).ifPresent(authenticationTokenRepository::delete);

        return authenticationTokenRepository.save(new AuthenticationToken(user)).getToken();
    }


    @Override
    public void removeAuthToken(String xAuth) {
        if (Strings.isNullOrEmpty(xAuth)) {
            throw new IllegalArgumentException("No X-Auth-Token present");
        }

        AuthenticationToken token =
                authenticationTokenRepository.findByToken(xAuth).orElseThrow(XAuthTokenNotFoundException::new);

        if (!token.isValid()) {
            throw new InvalidTokenException();
        }

        token.revoke();
        authenticationTokenRepository.saveAndFlush(token);
    }
}
