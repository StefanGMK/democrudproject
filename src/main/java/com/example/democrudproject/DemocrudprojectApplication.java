package com.example.democrudproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.democrudproject.repository")
@EntityScan(basePackages = "com.example.democrudproject.model")
public class DemocrudprojectApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemocrudprojectApplication.class, args);
	}

}
