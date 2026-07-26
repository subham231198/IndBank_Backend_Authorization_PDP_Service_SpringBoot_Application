package com.example.indbank.PolicyDecisionPointService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PolicyDecisionPointServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PolicyDecisionPointServiceApplication.class, args);
	}

}
