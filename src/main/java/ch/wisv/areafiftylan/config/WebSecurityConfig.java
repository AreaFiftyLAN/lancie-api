package ch.wisv.areafiftylan.config;

/**
 * Created by Wouter on 25-8-2015.
 */

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser("user").password("password").roles("USER")
                .and()
                .withUser("admin").password("password").roles("ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/users/**").authenticated();
        http.csrf().disable();
        http.httpBasic();
    }
}