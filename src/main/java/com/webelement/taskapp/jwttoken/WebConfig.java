package com.webelement.taskapp.jwttoken;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${client_file_path}")
	private String uploadPath;

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**") // allow all endpoints
						.allowedOrigins("http://localhost:4500" + "", "http://192.168.0.102:8081",
								"https://www.iba.org.in", "http://192.168.0.173:4200", "https://app.webelement.cc",
								"http://13.233.60.104", "https://app.hd-industries.com") // your Angular app URL
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS").allowedHeaders("*")
						.allowCredentials(true); // <--- VERY important for JSESSIONID
			}
		};
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadPath + "/");
	}

}
