package com.collaberadigital.cove;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan
@EnableJpaRepositories
//@EnableDiscoveryClient
public class CoveUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoveUserServiceApplication.class, args);
	}

}
