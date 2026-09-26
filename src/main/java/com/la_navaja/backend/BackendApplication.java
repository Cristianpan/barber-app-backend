package com.la_navaja.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.la_navaja.backend.infrastructure.config.AdminProperties;

@SpringBootApplication
@EnableConfigurationProperties(AdminProperties.class)
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
