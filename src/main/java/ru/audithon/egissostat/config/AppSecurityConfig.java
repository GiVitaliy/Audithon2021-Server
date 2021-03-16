package ru.audithon.egissostat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class AppSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    public AppSecurityConfig() {
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
            .antMatchers("/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().disable();

        http
            .cors()
            .and().csrf().disable()
            .authorizeRequests()
            .anyRequest()
            .authenticated();
    }
}
