package com.dongkap.security.configuration.entrypoints;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.dongkap.common.utils.ProfileType;

@Configuration
@Profile({ProfileType.DEVELOPMENT})
@Order(99)
public class AnonymousSwaggerConfiguration extends WebSecurityConfigurerAdapter {

	@Override
    public void configure(WebSecurity webSecurity) throws Exception {
       webSecurity.ignoring().antMatchers(
               // -- Swagger UI v2
               "/v2/api-docs",
               "/swagger-resources",
               "/swagger-resources/**",
               "/configuration/ui",
               "/configuration/security",
               "/swagger-ui.html",
               "/webjars/**",
               // -- Swagger UI v3 (OpenAPI)
               "/v3/api-docs/**",
               "/swagger-ui/**");
    }

}
