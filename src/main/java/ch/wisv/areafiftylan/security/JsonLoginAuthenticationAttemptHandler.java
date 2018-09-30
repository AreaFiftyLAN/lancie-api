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

package ch.wisv.areafiftylan.security;

import ch.wisv.areafiftylan.security.authentication.AuthenticationService;
import ch.wisv.areafiftylan.users.model.User;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JsonLoginAuthenticationAttemptHandler implements AuthenticationSuccessHandler, LogoutSuccessHandler {

    private final AuthenticationService authenticationService;
    private final Cache<String, Integer> attemptsCache;

    public JsonLoginAuthenticationAttemptHandler(AuthenticationService service) {
        this.authenticationService = service;
        // The attemptsCache automatically expires keys after 3 minutes
        attemptsCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(3, TimeUnit.MINUTES).build();
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Check if there were failed attempts in the past couple of minutes
        Integer previousAttempts = attemptsCache.getIfPresent(authentication.getName());
        if (previousAttempts != null && previousAttempts > 10) {
            attemptsCache.invalidate(authentication.getName());
            log.warn("Successful authentication after {} attempts", previousAttempts,
                    StructuredArguments.v("user_id", ((User) authentication.getPrincipal()).getId()));
        } else {
            log.info("Successful authentication for user {}", ((User) authentication.getPrincipal()).getId(),
                    StructuredArguments.v("user_id", ((User) authentication.getPrincipal()).getId()));
        }

        response.setHeader("X-Auth-Token", authenticationService.createNewAuthToken(authentication.getName()));
        response.setStatus(200);
    }

    /**
     * Register any authentication failures in the attemptscache
     *
     * @param username Username of failed authentication
     */
    public void onAuthenticationFailure(String username) {
        Integer attempts = this.attemptsCache.getIfPresent(username);
        if (attempts != null) {
            attemptsCache.put(username, attempts + 1);
        } else {
            attemptsCache.put(username, 1);
        }
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        authenticationService.removeAuthToken(request.getHeader("X-Auth-Token"));
        response.setStatus(200);
    }
}
