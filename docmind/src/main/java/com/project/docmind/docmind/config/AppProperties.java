package com.project.docmind.docmind.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix="app")
@Getter
@Setter
public class AppProperties {

	private RagProperties rag=new RagProperties();
	private CorsProperties cors=new CorsProperties();

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CorsProperties {
	private String allowedOrigins = "http://localhost:3000";
	private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
	private String allowedHeaders = "*";
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RagProperties{
		private int chunkSize=600;
		private int minChunkSizeChars=350;
//		private int chunkOverlap=100;
		private int minChunkLengthToEmbed=5;
		private int maxNumChunk=10000;
		private int topK=5;
		private double similarityThreshold=0.0;
	}
}
