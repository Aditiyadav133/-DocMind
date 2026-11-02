package com.project.docmind.docmind.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.docmind.docmind.dto.ApiResponse;
import com.project.docmind.docmind.dto.DocumentMetadataDto;
import com.project.docmind.docmind.dto.DocumentResponseDto;
import com.project.docmind.docmind.entity.User;
import com.project.docmind.docmind.service.DocumentMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(
	name="document management",
	description="endpoints for uploading, listing and managing documents and their vector embeddings "
	)
public class DocumentController {

	private final DocumentMetadataService documentService;
	private static final Logger logger=LoggerFactory.getLogger(DocumentController.class);

	public DocumentController(DocumentMetadataService documentService) {
		this.documentService=documentService;
	}

	@PostMapping(value="/upload",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary="upload and index documents")
	public  ResponseEntity<ApiResponse<DocumentResponseDto>> uploadDocument(@RequestParam("file") MultipartFile file,Authentication authentication){
		logger.info("document loading started");
		User user=(User) authentication.getPrincipal();
		DocumentResponseDto documentResponseDto=documentService.uploadAndProcess(file,user);
		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<DocumentResponseDto>(true,"document uploaded and indexed successfully",documentResponseDto,LocalDateTime.now()));
	}

	@PostMapping(value="/upload-multiple",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary="Upload and index multiple files simultaneosly",
	description="This api is used to upload and index multiple documents")
	public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> uploadMultiple(@RequestParam("files") List<MultipartFile> files,Authentication authentication){
		User user=(User) authentication.getPrincipal();
		List<DocumentResponseDto> responseDto=documentService.uploadMultipleDocuments(files,user);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<List<DocumentResponseDto>>builder()
				.message("Document uploaded successfully")
				.success(true)
				.timestamp(LocalDateTime.now())
				.data(responseDto)
				.build());
	}

//	Method fpr admin
	@GetMapping
	@PreAuthorize("hasRole('ADMIN') ")
	@Operation(summary="List all uploaded documents and their indexing status")
	public ResponseEntity<ApiResponse<List<DocumentMetadataDto>>> getAllDocuments(){
		List<DocumentMetadataDto> documents=documentService.getAllDocuments();
		return ResponseEntity.ok(ApiResponse.<List<DocumentMetadataDto>>builder()
				.message("All documents is here")
				.success(true)
				.timestamp(LocalDateTime.now())
				.data(documents)
				.build());
	}

	@GetMapping("/{id}")
	@Operation(summary="Getm metadata of a specific document by Id")
	public ResponseEntity<ApiResponse<DocumentMetadataDto>> getDocumentyId(@PathVariable UUID id){
		DocumentMetadataDto document=documentService.getDocumentById(id);
		return ResponseEntity.ok(ApiResponse.<DocumentMetadataDto>builder()
				.message("Selected document is here")
				.success(true)
				.timestamp(LocalDateTime.now())
				.data(document)
				.build());
	}

	@DeleteMapping("/{id}")
    @Operation(summary = "Delete a document and purge its vector embeddings from vector store")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>
                                builder()
                        .message("Document deleted successfully")
                        .success(true)
                        .timestamp(LocalDateTime.now())
                        .data(null)
                        .build()
        );
    }

	@GetMapping("/user")
	@Operation(summary="List all uploaded documents and their indexing status")
	public ResponseEntity<ApiResponse<List<DocumentMetadataDto>>> getAllDocumentsOfLoggerInUser(Authentication authentication){
		User user=(User) authentication.getPrincipal();
		List<DocumentMetadataDto> documents=documentService.getAllDocumentsByUser(user);
		return ResponseEntity.ok(ApiResponse.<List<DocumentMetadataDto>>builder().message("All documents are here").success(true).timestamp(LocalDateTime.now()).data(documents).build());
	}

}
