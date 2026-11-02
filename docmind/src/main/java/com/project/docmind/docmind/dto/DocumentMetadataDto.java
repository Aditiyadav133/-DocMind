package com.project.docmind.docmind.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.project.docmind.docmind.entity.DocumentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentMetadataDto {

	private UUID id;
	private  String filename;
	private  String contentType;
	private  Long fileSize;
	private Integer totalPages;
	private  Integer totalChunks;
	private DocumentStatus status;
	private  String errorMessage;
    private LocalDateTime createdAt;
    private  LocalDateTime updatedAt;
}
