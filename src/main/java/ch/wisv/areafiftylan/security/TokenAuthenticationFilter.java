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

package ch.wisv.areafiftylan.security;

import ch.wisv.areafiftylan.security.token.AuthenticationToken;
import ch.wisv.areafiftylan.security.token.repository.AuthenticationTokenRepository;
import ch.wisv.areafiftylan.users.model.User;
import com.google.common.base.Strings;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

class TokenAuthenticationFilter extends GenericFilterBean {

    private final AuthenticationTokenRepository authenticationTokenRepository;

    TokenAuthenticationFilter(AuthenticationTokenRepository authenticationTokenRepository) {
        this.authenticationTokenRepository = authenticationTokenRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String xAuth = ((HttpServletRequest) request).getHeader("X-Auth-Token");

        if (!Strings.isNullOrEmpty(xAuth)) {
            Optional<AuthenticationToken> authenticationTokenOptional =
                    authenticationTokenRepository.findByToken(xAuth);
            if (!authenticationTokenOptional.isPresent()) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token not found");
                return;
            } else {
                AuthenticationToken authenticationToken = authenticationTokenOptional.get();
                if (!authenticationToken.isValid()) {
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token Expired");
                    return;
                } else {
                    User user = authenticationToken.getUser();
                    // Add email to all logging for this request
                    MDC.put("user_id", user.getId().toString());
                    SecurityContextHolder.getContext().setAuthentication(
                            new PreAuthenticatedAuthenticationToken(user, "N/A", user.getAuthorities()));
                }
            }
        }

        chain.doFilter(request, response);
    }
}