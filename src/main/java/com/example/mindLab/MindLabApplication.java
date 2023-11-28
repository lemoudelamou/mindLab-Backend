package com.example.mindLab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@SpringBootApplication
@CrossOrigin(origins = "http://localhost:8080")

public class MindLabApplication {

	public static void main(String[] args) {
		SpringApplication.run(MindLabApplication.class, args);
	}



	@Configuration
	public class CorsConfig implements WebMvcConfigurer {

		@Override
		public void addCorsMappings(CorsRegistry registry) {
			registry.addMapping("/api/**") // Replace with your API path
					.allowedOrigins("http://localhost:3000") // Replace with the actual origin of your frontend
					.allowedMethods("GET", "POST", "PUT", "DELETE");
		}
	}
}
