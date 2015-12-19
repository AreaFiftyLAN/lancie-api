package ch.wisv.areafiftylan.security;

import com.allanditzel.springframework.security.web.csrf.CsrfTokenResponseHeaderBindingFilter;
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
import org.springframework.security.web.csrf.CsrfFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * This method is responsible for the main security configuration. The formlogin() section defines how to login.
     * POST requests should be made to /login with a username and password field. Errors are redirected to /login?error.
     * The logout section is similar.
     *
     * The last section is about permissions. Anything related to Login is accessible for everyone. Use this
     * for URL-based permissions if that's the best way. Use Method specific permissions if this is not feasible.
     *
     * By default, all requests to the API should come from authenticated sources. (USER or ADMIN)
     * @param http default parameter
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
                .loginPage("/login")
                .failureUrl("/login?error")
                .usernameParameter("username")
                .permitAll()
            .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            .and().authorizeRequests()
                .antMatchers("/mail").hasAuthority("ADMIN")
                .anyRequest().permitAll();
//                .anyRequest().authenticated();
        http.addFilterAfter(new CsrfTokenResponseHeaderBindingFilter(), CsrfFilter.class);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }

}