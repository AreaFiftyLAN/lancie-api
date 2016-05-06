package ch.wisv.areafiftylan.security;

import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.security.token.AuthenticationToken;
import ch.wisv.areafiftylan.service.repository.token.AuthenticationTokenRepository;
import com.google.common.base.Strings;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

            Authentication auth = authenticationToken.getAuthenticationToken();
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);

    }
}


//
//import ch.wisv.areafiftylan.exception.TokenNotFoundException;
//import ch.wisv.areafiftylan.security.token.AuthenticationToken;
//import ch.wisv.areafiftylan.service.repository.token.AuthenticationTokenRepository;
//import com.google.common.base.Strings;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
//import org.springframework.web.filter.GenericFilterBean;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//
///**
// * Created by Sille Kamoen on 6-5-16.
// */
//public class TokenAuthenticationFilter extends GenericFilterBean {
//
//    private AuthenticationTokenRepository authenticationTokenRepository;
//    private AuthenticationManager authenticationManager;
//
//    TokenAuthenticationFilter(AuthenticationTokenRepository authenticationTokenRepository) {
//        this.authenticationTokenRepository = authenticationTokenRepository;
//    }
//
//    @Override
//    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
//            throws IOException, ServletException {
//        HttpServletRequest request = (HttpServletRequest) req;
//        HttpServletResponse response = (HttpServletResponse) res;
//
//        String token = request.getHeader("X-Auth-Token");
//        if (!Strings.isNullOrEmpty(token)) {
//            AuthenticationToken authenticationToken = authenticationTokenRepository.findByToken(token)
//                    .orElseThrow(() -> new TokenNotFoundException(token));
//
//            authenticationManager
//                    .authenticate(new PreAuthenticatedAuthenticationToken(authenticationToken.getUser(), null));
//
//            //            SecurityContextHolder.getContext().setAuthentication();
//        }
//        chain.doFilter(req, res);
//    }
//
//    @Override
//    public void destroy() {
//
//    }
//}
