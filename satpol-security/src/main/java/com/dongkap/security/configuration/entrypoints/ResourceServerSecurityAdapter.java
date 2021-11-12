package com.dongkap.security.configuration.entrypoints;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.dongkap.common.utils.ResourceCode;

public class ResourceServerSecurityAdapter extends ResourceServerConfigurerAdapter {

    private TokenStore tokenStore;
    private AccessDeniedHandler accessDeniedHandler;
    private AuthenticationEntryPoint authenticationEntryPoint;

    private String resourceId = ResourceCode.SECURITY;
    private String resourcePath = ResourceCode.SECURITY_PATH;
    
    public ResourceServerSecurityAdapter() {}
    public ResourceServerSecurityAdapter(TokenStore tokenStore, AccessDeniedHandler accessDeniedHandler, AuthenticationEntryPoint authenticationEntryPoint) {
		this.tokenStore = tokenStore;
		this.accessDeniedHandler = accessDeniedHandler;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        // @formatter:off
        resources
	    	.resourceId(resourceId)
	    	.tokenStore(tokenStore)
	    	.authenticationEntryPoint(authenticationEntryPoint)
	    	.accessDeniedHandler(accessDeniedHandler);
        // @formatter:on
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
        .exceptionHandling().accessDeniedHandler(accessDeniedHandler).and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        .csrf().disable()
        .requestMatchers()
    	.antMatchers(					resourcePath+ "/**").and()
        .authorizeRequests()
        .antMatchers(HttpMethod.GET,	resourcePath+ "/vw/get/**")
    		.access("#oauth2.hasScope('read')")
        .antMatchers(HttpMethod.GET,	resourcePath+ "/vw/param/**")
    		.access("#oauth2.hasScope('read')")
        .antMatchers(HttpMethod.POST,	resourcePath+ "/vw/post/**")
        	.access("#oauth2.hasScope('read')")
        .antMatchers(HttpMethod.POST,	resourcePath+ "/vw/param/**")
        	.access("#oauth2.hasScope('read')")
        .antMatchers(HttpMethod.POST,	resourcePath+ "/trx/add/**")
        	.access("#oauth2.hasScope('write')")
        .antMatchers(HttpMethod.POST,	resourcePath+ "/trx/post/**")
        	.access("#oauth2.hasScope('write')")
        .antMatchers(HttpMethod.POST,	resourcePath+ "/trx/delete/**")
        	.access("#oauth2.hasScope('write')")
        .antMatchers(HttpMethod.PUT,	resourcePath+ "/trx/put/**")
        	.access("#oauth2.hasScope('write')")
        .antMatchers(HttpMethod.POST,	resourcePath+ "/oauth/**")
        	.access("#oauth2.hasScope('read')")
        .antMatchers(HttpMethod.GET,	resourcePath+ "/oauth/**")
        	.access("#oauth2.hasScope('read')")
        .antMatchers(HttpMethod.DELETE,	resourcePath+ "/oauth/**")
        	.access("#oauth2.hasScope('read')")
        .antMatchers(HttpMethod.PUT,	resourcePath+ "/oauth/**")
        	.access("#oauth2.hasScope('read')")
        .antMatchers(HttpMethod.GET,	resourcePath+ "/vw/auth/**")
			.access("#oauth2.hasScope('trust') and hasAnyAuthority('SYS_ADMIN')")
        .antMatchers(HttpMethod.POST,	resourcePath+ "/vw/auth/**")
    		.access("#oauth2.hasScope('trust') and hasAnyAuthority('SYS_ADMIN')")
        .antMatchers(HttpMethod.POST,	resourcePath+ "/trx/auth/**")
    		.access("#oauth2.hasScope('trust') and hasAnyAuthority('SYS_ADMIN')")
        .antMatchers(HttpMethod.DELETE,	resourcePath+ "/trx/auth/**")
    		.access("#oauth2.hasScope('trust') and hasAnyAuthority('SYS_ADMIN')")
        .anyRequest().denyAll()
        .and()
    		.oauth2Login();
        // @formatter:on
    }
}
