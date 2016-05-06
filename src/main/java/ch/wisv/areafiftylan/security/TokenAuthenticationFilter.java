package ch.wisv.areafiftylan.security;

import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.security.token.AuthenticationToken;
import ch.wisv.areafiftylan.service.repository.token.AuthenticationTokenRepository;
import com.google.common.base.Strings;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

class TokenAuthenticationFilter extends GenericFilterBean {

    private AuthenticationTokenRepository authenticationTokenRepository;

    TokenAuthenticationFilter(AuthenticationTokenRepository authenticationTokenRepository) {
        this.authenticationTokenRepository = authenticationTokenRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String xAuth = ((HttpServletRequest) request).getHeader("X-Auth-Token");

        if (!Strings.isNullOrEmpty(xAuth)) {
            AuthenticationToken authenticationToken = authenticationTokenRepository.findByToken(xAuth)
                    .orElseThrow(() -> new TokenNotFoundException(xAuth));

            if (authenticationToken.isValid()) {
                Authentication auth = authenticationToken.getAuthenticationToken();
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                throw new NonceExpiredException("Token expired!");
            }
        }

        chain.doFilter(request, response);
    }
}