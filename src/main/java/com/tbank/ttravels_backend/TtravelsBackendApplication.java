package com.tbank.ttravels_backend;

import com.tbank.ttravels_backend.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class TtravelsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TtravelsBackendApplication.class, args);
	}

}
