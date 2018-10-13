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
import ch.wisv.areafiftylan.security.token.repository.AuthenticationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
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
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    public static final String HIERARCHY =
            "ROLE_ADMIN > ROLE_COMMITTEE and ROLE_COMMITTEE > ROLE_OPERATOR and ROLE_OPERATOR > ROLE_USER";

    private final UserDetailsService userDetailsService;

    private final AuthenticationTokenRepository authenticationTokenRepository;

    private AuthenticationService authenticationService;

    @Value("${a5l.ratelimit.minutes:10}")
    private int MAX_ATTEMPTS_MINUTE;
    @Value("${a5l.ratelimit.enabled:true}")
    private boolean RATELIMIT_ENABLED;

    @Autowired
    public SecurityConfiguration(AuthenticationTokenRepository authenticationTokenRepository,
                                 @Qualifier("userServiceImpl") UserDetailsService userDetailsService,
                                 AuthenticationService authenticationService) {
        this.authenticationTokenRepository = authenticationTokenRepository;
        this.userDetailsService = userDetailsService;
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
     *
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        JsonLoginAuthenticationAttemptHandler attemptHandler =
                new JsonLoginAuthenticationAttemptHandler(authenticationService);
        http.logout().logoutSuccessHandler(attemptHandler);

        http.authorizeRequests().expressionHandler(webExpressionHandler()).requestMatchers(EndpointRequest.to("health"))
                .permitAll().requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN").anyRequest().permitAll();

        // We use custom Authentication Tokens, making csrf redundant
        http.csrf().disable();

        // Add the default headers first
        http.addFilterBefore(new CORSFilter(), JsonLoginFilter.class);

        // Set the login point to get X-Auth-Tokens
        JsonLoginFilter jsonLoginFilter = new JsonLoginFilter(this.authenticationManagerBean(), attemptHandler);
        JsonLoginFilter.setMAX_ATTEMPTS_MINUTE(MAX_ATTEMPTS_MINUTE);
        JsonLoginFilter.setRATELIMIT_ENABLED(RATELIMIT_ENABLED);

        http.addFilterAfter(jsonLoginFilter, UsernamePasswordAuthenticationFilter.class);
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
        roleHierarchy.setHierarchy(HIERARCHY);
        return roleHierarchy;
    }

    private SecurityExpressionHandler<FilterInvocation> webExpressionHandler() {
        DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy());
        return handler;
    }
}

