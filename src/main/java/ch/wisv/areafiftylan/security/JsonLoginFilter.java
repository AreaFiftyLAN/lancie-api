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

import ch.wisv.areafiftylan.users.model.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.TimeUnit;

/**
 * This Login filter uses the default "Form" login filter, but parses a JSON requestbody instead. It accepts requests on
 * /login and returns an X-Auth-Token Header on successful authentication using the
 * JsonLoginAuthenticationAttemptHandler
 */
@Slf4j
public class JsonLoginFilter extends UsernamePasswordAuthenticationFilter {

    private UserDTO userDTO = new UserDTO();
    private AuthenticationManager authenticationManager;
    private JsonLoginAuthenticationAttemptHandler attemptHandler;
    private final Cache<String, Integer> attemptsCache;

    @Setter
    public static int MAX_ATTEMPTS_MINUTE;
    @Setter
    public static boolean RATELIMIT_ENABLED;


    public JsonLoginFilter(AuthenticationManager authenticationManager,
                           JsonLoginAuthenticationAttemptHandler successHandler) {
        super();
        this.authenticationManager = authenticationManager;
        this.attemptHandler = successHandler;
        attemptsCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(5, TimeUnit.MINUTES).build();
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        userDTO = getUserDTO(request);

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(userDTO.getEmail(), userDTO.getPassword());

        // Allow subclasses to set the "details" property
        setDetails(request, token);

        if (!RATELIMIT_ENABLED || addAttempt(request)) {
            return this.authenticationManager.authenticate(token);
        } else {
            throw new AuthenticationServiceException("IP Address blocked");
        }
    }

    private boolean addAttempt(HttpServletRequest request) {
        String ip = getClientIP(request);

        Integer attempts = this.attemptsCache.getIfPresent(ip);
        if (attempts != null) {
            attempts++;
        } else {
            attempts = 1;
        }
        attemptsCache.put(ip, attempts);

        if (attempts > MAX_ATTEMPTS_MINUTE) {
            log.warn("Blocking IP address {}", ip);
            return false;
        }
        return true;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        attemptsCache.invalidate(getClientIP(request));
        attemptHandler.onAuthenticationSuccess(request, response, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        // Register failed attempts
        attemptHandler.onAuthenticationFailure(userDTO.getEmail());
        super.unsuccessfulAuthentication(request, response, failed);
    }

    private UserDTO getUserDTO(HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Reader reader = request.getReader();
            return mapper.readValue(reader, UserDTO.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cant read request data");
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        // Proxies sometimes store the entire path. We want the first step only
        return xfHeader.split(",")[0];
    }
}
