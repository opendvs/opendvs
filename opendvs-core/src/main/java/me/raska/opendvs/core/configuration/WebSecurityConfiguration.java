package me.raska.opendvs.core.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import me.raska.opendvs.core.service.UserSecurityService;

@Configuration
@EnableOAuth2Sso
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserSecurityService userSecurityService;

    @Autowired
    private ApiTokenInterceptorFilter apiTokenInterceptorFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests().antMatchers("/login").permitAll().anyRequest().authenticated().and().formLogin()
                .loginPage("/login").permitAll().and().logout().logoutSuccessUrl("/login").permitAll();

        http.addFilterBefore(apiTokenInterceptorFilter, AbstractPreAuthenticatedProcessingFilter.class);
    }

    @Bean
    public AuthoritiesExtractor authoritiesExtractor() {
        return userSecurityService::generateUserSession;
    }
}
