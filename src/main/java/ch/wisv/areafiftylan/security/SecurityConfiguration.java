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

import ch.wisv.areafiftylan.security.authentication.AuthenticationService;
import ch.wisv.areafiftylan.security.token.repository.AuthenticationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(ManagementServerProperties.ACCESS_OVERRIDE_ORDER)
class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String hierarchy = "ROLE_ADMIN > ROLE_COMMITTEE and " +
                                            "ROLE_COMMITTEE > ROLE_OPERATOR and " +
                                            "ROLE_OPERATOR > ROLE_USER";

    private final UserDetailsService userDetailsService;

    private final AuthenticationTokenRepository authenticationTokenRepository;

    private final RESTAuthenticationEntryPoint authenticationEntryPoint;

    private AuthenticationService authenticationService;

    @Autowired
    public SecurityConfiguration(AuthenticationTokenRepository authenticationTokenRepository,
                                 UserDetailsService userDetailsService,
                                 RESTAuthenticationEntryPoint authenticationEntryPoint,
                                 AuthenticationService authenticationService) {
        this.authenticationTokenRepository = authenticationTokenRepository;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.authenticationService = authenticationService;
    }

    /**
     * This method is responsible for the main security configuration.
     * POST requests should be made to /login with an email and password field.
     * The logout section is similar.
     * <p>
     * The last section is about permissions. Anything related to Login is accessible for everyone. Use this for
     * URL-based permissions if that's the best way. Use Method specific permissions if this is not feasible.
     * <p>
     * By default, all requests to the API should come from authenticated sources. (USER or ADMIN)
     *
     * @param http default parameter
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // We use our own exception handling for unauthorized request. THis simply returns a 401 when a request
        // should have been authenticated.
        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.authorizeRequests().expressionHandler(webExpressionHandler());

        // We use custom Authentication Tokens, making csrf redundant
        http.csrf().disable();

        // Add the default headers first
        http.addFilterBefore(new CORSFilter(), JsonLoginFilter.class);
        // Set the login point to get X-Auth-Tokens
        http.addFilterAfter(new JsonLoginFilter(this.authenticationManagerBean(),
                        new JsonLoginAuthenticationSuccessHandler(authenticationService)),
                UsernamePasswordAuthenticationFilter.class);
        // Add support for Token-base authentication
        http.addFilterAfter(new TokenAuthenticationFilter(authenticationTokenRepository),
                UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Bean
    public RoleHierarchyImpl roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }

    @Bean
    protected RoleVoter roleVoter(RoleHierarchy roleHierarchy) {
        return new RoleHierarchyVoter(roleHierarchy);
    }

    private SecurityExpressionHandler<FilterInvocation> webExpressionHandler() {
        DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy());
        return handler;
    }
}

