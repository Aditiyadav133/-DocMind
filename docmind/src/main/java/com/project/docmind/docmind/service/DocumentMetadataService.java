package com.project.docmind.docmind.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.project.docmind.docmind.dto.DocumentMetadataDto;
import com.project.docmind.docmind.dto.DocumentResponseDto;
import com.project.docmind.docmind.entity.DocumentMetadata;
import com.project.docmind.docmind.entity.DocumentStatus;
import com.project.docmind.docmind.entity.User;
import com.project.docmind.docmind.exception.DocumentProcessingException;
import com.project.docmind.docmind.exception.ResourceNotFoundException;
import com.project.docmind.docmind.repository.DocumentMetadataRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentMetadataService {

	private static final Logger logger=LoggerFactory.getLogger(DocumentMetadataService.class);

	private final DocumentMetadataRepo documentMetadataRepo;
	private final JdbcTemplate jdbcTemplate;
	private final DocumentParserService parserService;
	private final DocumentIngestionService ingestionService;
	private final ModelMapper modelMapper;

	@Transactional
	public DocumentResponseDto uploadAndProcess(MultipartFile file, User user) {
		String fileName=file.getOriginalFilename()!=null?file.getOriginalFilename(): "document";
		String contentType=file.getContentType()!=null?file.getContentType(): "application/octat-stream";
		DocumentMetadata documentMetadata= DocumentMetadata.builder()
				.filename(fileName).createdAt(LocalDateTime.now()).contentType(contentType).status(DocumentStatus.UPLOADING).fileSize(file.getSize())
				.user(user).build();
		documentMetadata=documentMetadataRepo.save(documentMetadata);
		int chunksCreated=0;
		try {
			List<Document> parsedDocs=parserService.parse(file);
			chunksCreated=ingestionService.ingest(documentMetadata,parsedDocs);
		}catch(DocumentProcessingException ex) {
			logger.info("Document metadata deleting due to fail processing");
			documentMetadataRepo.delete(documentMetadata);
			throw ex;
		}

//		documentMetadata.setTotalChunks(chunksCreated);

		return DocumentResponseDto.builder().id(documentMetadata.getId()).filename(documentMetadata.getFilename()).fileSize(documentMetadata.getFileSize()).chunksCreated(chunksCreated).status(documentMetadata.getStatus()).message("Document successfully processed and indexed.").userId(user.getId()).build();
	}

	public List<DocumentResponseDto> uploadMultipleDocuments(List<MultipartFile> files,User user) {
		List<DocumentResponseDto> responseDto=new ArrayList<>();
		for(MultipartFile file:files) {
			DocumentResponseDto result=uploadAndProcess(file,user);
			responseDto.add(result);
		}
		return responseDto;
	}

	public List<DocumentMetadataDto> getAllDocuments() {
		List<DocumentMetadata> allDocuments=documentMetadataRepo.findAllByOrderByCreatedAtDesc();
		return allDocuments.stream().map(documentMetadata -> modelMapper.map(documentMetadata,DocumentMetadataDto.class)).toList();
	}

	public DocumentMetadataDto getDocumentById(UUID id) {
		DocumentMetadata documentMetadata=documentMetadataRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource with given id not found !!"));
		return modelMapper.map(documentMetadata, DocumentMetadataDto.class);
	}

	@Transactional
	public void deleteDocument(UUID id) {
	    DocumentMetadata documentMetadata =
	        documentMetadataRepo.findById(id)
	            .orElseThrow(() ->
	                new ResourceNotFoundException(
	                    "Document with given id not found !!"
	                )
	            );
	    String sql =
	        "DELETE FROM public.vector_store " +
	        "WHERE metadata->>'documentId' = ?";
	    int deletedCount =
	        jdbcTemplate.update(sql, id.toString());
	    logger.info(
	        "Deleted {} vector chunks for document {}",
	        deletedCount,
	        id
	    );
	    documentMetadataRepo.delete(documentMetadata);
	}

	public List<DocumentMetadataDto> getAllDocumentsByUser(User user){
			List<DocumentMetadata> documents=documentMetadataRepo.findByUserOrderByCreatedAtDesc(user);
			return documents.stream().map(documentMetadata -> modelMapper.map(documentMetadata, DocumentMetadataDto.class)).toList();
		}
}
