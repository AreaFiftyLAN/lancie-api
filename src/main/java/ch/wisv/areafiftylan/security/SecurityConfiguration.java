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

import ch.wisv.areafiftylan.security.token.repository.AuthenticationTokenRepository;
import com.allanditzel.springframework.security.web.csrf.CsrfTokenResponseHeaderBindingFilter;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;

    private final AuthenticationTokenRepository authenticationTokenRepository;

    private final RESTAuthenticationEntryPoint authenticationEntryPoint;

    private final RESTAuthenticationSuccessHandler authenticationSuccessHandler;

    private final RESTAuthenticationFailureHandler authenticationFailureHandler;

    @Autowired
    public SecurityConfiguration(AuthenticationTokenRepository authenticationTokenRepository,
                                 RESTAuthenticationFailureHandler authenticationFailureHandler,
                                 UserDetailsService userDetailsService,
                                 RESTAuthenticationSuccessHandler authenticationSuccessHandler,
                                 RESTAuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationTokenRepository = authenticationTokenRepository;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.userDetailsService = userDetailsService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /**
     * This method is responsible for the main security configuration. The formlogin() section defines how to login.
     * POST requests should be made to /login with a username and password field. Errors are redirected to /login?error.
     * The logout section is similar.
     * <p>
     * The last section is about permissions. Anything related to Login is accessible for everyone. Use this for
     * URL-based permissions if that's the best way. Use Method specific permissions if this is not feasible.
     * <p>
     * By default, all requests to the API should come from authenticated sources. (USER or ADMIN)
     *
     * @param http default parameter
     *
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // We use our own exception handling for unauthorized request. THis simply returns a 401 when a request
        // should have been authenticated.
        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);

        //@formatter:off
        http.formLogin()
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
            .and()
                .logout()
                .logoutUrl("/logout");
        //@formatter:on

        http.csrf().
                // This is used for the Mollie webhook, so it shouldn't be protected by CSRF
                        ignoringAntMatchers("/orders/status").
                // Don't require CSRF on requests with valid Tokens
                        requireCsrfProtectionMatcher(csrfRequestMatcher).
                // We also ignore this for Token requests
                        ignoringAntMatchers("/token").
                // Ignore the route to request a password reset, no CSRF protection is needed
                        ignoringAntMatchers("/requestResetPassword");
        //@formatter:on

        // This is the filter that adds the CSRF Token to the header. CSRF is enabled by default in Spring, this just
        // copies the content to the X-CSRF-TOKEN header field.
        http.addFilterAfter(new CsrfTokenResponseHeaderBindingFilter(), CsrfFilter.class);

        // Add support for Token-base authentication
        http.addFilterAfter(new TokenAuthenticationFilter(authenticationTokenRepository),
                UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }

    // We want to enable CSRF for State Changing methods. We don't want CSRF for requests with valid tokens.
    private RequestMatcher csrfRequestMatcher = new RequestMatcher() {
        private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

        @Override
        public boolean matches(HttpServletRequest request) {
            String requestHeader = request.getHeader("X-Auth-Token");
            boolean methodAllowed = !(allowedMethods.matcher(request.getMethod()).matches());

            return Strings.isNullOrEmpty(requestHeader) && methodAllowed;
        }
    };
}

