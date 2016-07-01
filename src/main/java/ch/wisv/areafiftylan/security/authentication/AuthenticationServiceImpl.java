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

import ch.wisv.areafiftylan.security.token.AuthenticationToken;
import ch.wisv.areafiftylan.security.token.repository.AuthenticationTokenRepository;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserService;
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

            return authenticationTokenRepository.save(new AuthenticationToken(user)).getToken();
        }

        throw new AuthenticationCredentialsNotFoundException("Incorrect credentials");
    }

    private boolean correctCredentials(User user, String password) {
        return new BCryptPasswordEncoder().matches(password, user.getPassword());
    }
}
