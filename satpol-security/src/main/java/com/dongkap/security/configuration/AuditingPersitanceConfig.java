package com.dongkap.security.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingPersitanceConfig {

	@Bean("auditorProvider")
	public AuditorAware<String> auditorProvider(){
		return new AuditorAwareImpl();
	}
}
