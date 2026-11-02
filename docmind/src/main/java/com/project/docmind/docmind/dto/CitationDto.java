package com.project.docmind.docmind.dto;

import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitationDto {

	 private UUID documentId;
	    private String fileName;
	    private Integer chunkIndex;
	    private Integer pageNumber;
	    private String snippet;
	    private Double similarityScore;
	    private Map<String, Object> metadata;

}
