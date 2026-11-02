package com.project.docmind.docmind.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

	private final AppProperties appProperties;

	@Override
	public void addCorsMappings(CorsRegistry registry) {

		String[] allowedOrigins=appProperties.getCors().getAllowedOrigins().split(",");
		String[] allowedMethods=appProperties.getCors().getAllowedMethods().split(",");
		String[] allowedHeaders=appProperties.getCors().getAllowedHeaders().split(",");
		registry.addMapping("/api/**").allowedOrigins(allowedOrigins).allowedMethods(allowedMethods).allowedHeaders(allowedHeaders).allowCredentials(true);

	}

}
