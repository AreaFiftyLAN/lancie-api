/*
 * Copyright (c) 2017  W.I.S.V. 'Christiaan Huygens'
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;

/**
 * This Login filter uses the default "Form" login filter, but parses a JSON requestbody instead. It accepts requests on
 * /login and returns an X-Auth-Token Header on successful authentication using the
 * JsonLoginAuthenticationSuccessHandler
 */
public class JsonLoginFilter extends UsernamePasswordAuthenticationFilter {

    private UserDTO userDTO = new UserDTO();
    private AuthenticationManager authenticationManager;
    private JsonLoginAuthenticationSuccessHandler successHandler;

    public JsonLoginFilter(AuthenticationManager authenticationManager,
                           JsonLoginAuthenticationSuccessHandler successHandler) {
        super();
        this.authenticationManager = authenticationManager;
        this.successHandler = successHandler;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        userDTO = getUserDTO(request);
        return super.attemptAuthentication(request, response);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        successHandler.onAuthenticationSuccess(request, response, authResult);
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

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        return userDTO.getEmail();
    }

    @Override
    protected String obtainPassword(HttpServletRequest request) {
        return userDTO.getPassword();
    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
        return this.authenticationManager;
    }

    @Override
    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
        super.setAuthenticationSuccessHandler(successHandler);
    }
}
