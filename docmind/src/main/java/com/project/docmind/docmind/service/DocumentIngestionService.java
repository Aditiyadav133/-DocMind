package com.project.docmind.docmind.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.project.docmind.docmind.config.AppProperties;
import com.project.docmind.docmind.entity.DocumentMetadata;
import com.project.docmind.docmind.entity.DocumentStatus;
import com.project.docmind.docmind.exception.DocumentProcessingException;
import com.project.docmind.docmind.repository.DocumentMetadataRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

	private static final Logger logger=LoggerFactory.getLogger(DocumentIngestionService.class);
	private final DocumentMetadataRepo documentMetadataRepo;
	private final VectorStore vectorStore;
	private final AppProperties appProperties;

	public int ingest(DocumentMetadata metadata, List<Document> parsedDocs) {
		logger.info("Ingesting document [id={}, name={}, pages={}]", metadata.getId(),metadata.getFilename(),metadata.getFileSize());
		try {
			metadata.setStatus(DocumentStatus.PROCESSING);
			metadata.setTotalPages(parsedDocs.size());

			TokenTextSplitter tokenTextSplitter=TokenTextSplitter.builder().withChunkSize(appProperties.getRag().getChunkSize()).withChunkSize(appProperties.getRag().getChunkSize()).withMinChunkSizeChars(appProperties.getRag().getMinChunkSizeChars()).withMinChunkLengthToEmbed(appProperties.getRag().getMinChunkLengthToEmbed()).withMaxNumChunks(appProperties.getRag().getMaxNumChunk()).withKeepSeparator(true).build();
			List<Document> chunks=tokenTextSplitter.apply(parsedDocs);
			if(chunks.isEmpty()) {
				metadata.setStatus(DocumentStatus.FAILED);
				metadata.setErrorMessage("Document appears to be empty or unscannable");
				documentMetadataRepo.save(metadata);
				return 0;
			}

//			metadata enrichment on each chunk

			List<Document> enrichedChunks=new ArrayList<>();
			for(int i=0;i<chunks.size();i++) {
				Document chunk=chunks.get(i);
				Map<String,Object> enrichedMetadata=new HashMap<>(chunk.getMetadata());
				enrichedMetadata.put("documentId", metadata.getId().toString());
				enrichedMetadata.put("fileName", metadata.getFilename());
				enrichedMetadata.put("contentType", metadata.getFilename());
				enrichedMetadata.put("chunkIndex", i);
				enrichedMetadata.put("userId", metadata.getUser().getId().toString());
				Object pageNumber=chunk.getMetadata().get("pageNumber");
				if(pageNumber==null) {
					pageNumber=chunk.getMetadata().get("pageNumber");
				}
				if(pageNumber!=null) {
					enrichedMetadata.put("pageNumber", pageNumber);
				}
				Document enrichedDoc=new Document(chunk.getText(),enrichedMetadata);
				enrichedChunks.add(enrichedDoc);
			}

//			write chunks and embeddings to PgVector

			logger.info("Writing {} vector chunks to PgVector for documents: {}",enrichedChunks.size(),metadata.getFilename());
			vectorStore.add(enrichedChunks);

//			update document status to indexed
			metadata.setStatus(DocumentStatus.INDEXED);
			metadata.setTotalChunks(enrichedChunks.size());
			metadata.setErrorMessage(null);
			documentMetadataRepo.save(metadata);
			return enrichedChunks.size();

		}catch(Exception ex) {
			logger.info("Failed to ingest document into vector store: {}",metadata.getFilename(),ex);
			metadata.setStatus(DocumentStatus.FAILED);
			metadata.setErrorMessage(ex.getMessage());
			documentMetadataRepo.save(metadata);
			throw new DocumentProcessingException("Failed to index document: "+ex.getMessage(),ex);
		}
	}

}
