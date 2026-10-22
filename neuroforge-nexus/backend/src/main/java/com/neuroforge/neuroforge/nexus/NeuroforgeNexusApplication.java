package com.neuroforge.neuroforge.nexus;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NeuroforgeNexusApplication {

	@PostConstruct
	public void init() {
		// Enables SecurityContext propagation to async/reactive worker threads
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
	}

	public static void main(String[] args) {
		SpringApplication.run(NeuroforgeNexusApplication.class, args);
	}

}
