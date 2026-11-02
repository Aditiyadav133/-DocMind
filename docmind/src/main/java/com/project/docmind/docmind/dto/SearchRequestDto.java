package com.project.docmind.docmind.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
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
public class SearchRequestDto {

	  @NotBlank(message = "Query cannot be empty")
	    private String query;

	    private UUID documentId;

	    private  Integer topK;

	    private  Double similaritySearch;
}
