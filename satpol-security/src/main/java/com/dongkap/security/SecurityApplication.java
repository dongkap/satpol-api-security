package com.dongkap.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

import com.dongkap.security.configuration.ApplicationProperties;

@SpringBootApplication(scanBasePackages={"com.dongkap"})
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableAutoConfiguration
@EnableResourceServer
@EnableFeignClients(basePackages = {"com.dongkap.feign"})
@EnableCircuitBreaker
//@EnableCaching
public class SecurityApplication extends SpringBootServletInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(SecurityApplication.class);
	
    public static void main(String[] args) {
	    SpringApplication.run(SecurityApplication.class, args);
        LOG.info("DONGKAP - Hello Satpol!");
    }

}
