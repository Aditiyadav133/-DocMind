package com.project.docmind.docmind.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import com.project.docmind.docmind.config.AppProperties;
import com.project.docmind.docmind.dto.ChatRequestDto;
import com.project.docmind.docmind.dto.ChatResponseDto;
import com.project.docmind.docmind.dto.CitationDto;
import com.project.docmind.docmind.dto.SearchRequestDto;
import com.project.docmind.docmind.dto.SearchResultDto;
import com.project.docmind.docmind.entity.Conversation;
import com.project.docmind.docmind.entity.User;
import com.project.docmind.docmind.repository.ConversationRepository;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class RagService {

	private static final Logger logger=LoggerFactory.getLogger(RagService.class);
	private final VectorStore vectorStore;
	private final AppProperties appProperties;
	private final ChatClient chatClient;
	private final ConversationRepository conversationRepository;
	public SearchResultDto searchSimilarChunks(SearchRequestDto request,User user) {
		List<Document> matchedDocs=retrieveRelevantDocuments(request.getQuery(),request.getDocumentId(),request.getTopK(),request.getSimilaritySearch(),user);
		List<CitationDto> citations=matchedDocs.stream().map(this::mapToCitation).toList();
		return SearchResultDto.builder().query(request.getQuery()).totalMatches(citations.size()).matches(citations).build();
	}

	private List<Document> retrieveRelevantDocuments(@NotBlank(message="Query cannot be empty") String query, UUID documentId, Integer topK,
			Double similaritySearch,User user) {
		int effectiveTopK=(topK!=null && topK>0)?topK:appProperties.getRag().getTopK();
		double effectiveSimilaritySearch =(similaritySearch!=null)?similaritySearch:appProperties.getRag().getSimilarityThreshold();
		SearchRequest.Builder searchRequestBuilder=SearchRequest.builder().query(query).topK(effectiveTopK);
		if(effectiveSimilaritySearch>0.0) {
			searchRequestBuilder.similarityThreshold(effectiveSimilaritySearch);
		}
		FilterExpressionBuilder b = new FilterExpressionBuilder();
		if (documentId != null) {
			logger.info("Searching from document");
		    Filter.Expression documentId1=b.and(b.eq("userId", user.getId().toString()),b.eq("documentId", documentId.toString())).build();
		    searchRequestBuilder.filterExpression(documentId1);
		}
		else {
			Filter.Expression userIdFilter=b.eq("userId", user.getId().toString()).build();
			searchRequestBuilder.filterExpression(userIdFilter);
		}
		try {
			List<Document> documents=vectorStore.similaritySearch(searchRequestBuilder.build());
			logger.info("Retrieved {} chunks for query: '{}' {scoped docId: {}}",documents.size(),query,documentId);
			return documents;
		}
		catch(Exception e) {
			logger.error("Similarity search failed for query: '{}",query,e);
			return Collections.emptyList();
		}
	}

	private CitationDto mapToCitation(Document document) {
		Map<String,Object> meta=document.getMetadata();
		UUID docId=null;
		if(meta.get("documentId")!=null) {
			try {
				docId=UUID.fromString(meta.get("documentId").toString());

			}catch(Exception ignore) {

			}
		}
		Integer chunkIndex=null;
		if(meta.get("chunkIndex") instanceof Number n) {
			chunkIndex=n.intValue();
		}
		Integer pageNumber=null;
		if(meta.get("pageNumber") instanceof Number n) {
			pageNumber=n.intValue();
		}
		else if(meta.get("page_number") instanceof Number n) {
			pageNumber=n.intValue();
		}
		Double score=null;
		if(meta.get("distance") instanceof Number n) {
			score=1.0 - n.doubleValue();
		}
		return CitationDto.builder().documentId(docId).fileName((String) meta.getOrDefault("fileName", "Unknown")).chunkIndex(chunkIndex).pageNumber(pageNumber).snippet(document.getText()).similarityScore(score).metadata(meta).build();
	}

	public ChatResponseDto askQuestion(ChatRequestDto request,User user) {

		String conversationId=request.getConversationId()!=null?request.getConversationId():UUID.randomUUID().toString();
		ensureConversationExists(conversationId,user,request.getQuestion());
		long startTime=System.currentTimeMillis();
		logger.info("Processing query: '{}' , scoped documentId: {}",request.getQuestion(),request.getDocumentId());
		List<Document> similarDocuments=retrieveRelevantDocuments(request.getQuestion(),request.getDocumentId(),request.getTopK(),request.getMinSimilarity(),user);
		List<CitationDto> citationDtos=similarDocuments.stream().map(this::mapToCitation).toList();
		String contextText=buildContextString(similarDocuments);
//		String prompt=buildPrompt(request.getQuestion(),contextText);
		String answer=chatClient.prompt()
				.system(system -> system.param("doc_context", contextText))
				.user(request.getQuestion()).advisors(a -> a.param(ChatMemory.CONVERSATION_ID,request.getConversationId())).call().content();
		long responseTime=System.currentTimeMillis()-startTime;
		logger.info("Completed Q&A in {}ms with {} citations",responseTime,citationDtos.size());
		return ChatResponseDto.builder().answer(answer).conversationId(request.getConversationId()!=null ? request.getConversationId():UUID.randomUUID().toString()).citations(citationDtos).responseTimeMs(responseTime).build();
	}

	private void ensureConversationExists(String conversationId, User user, String question) {
		 conversationRepository.findByIdAndUser(conversationId, user)
         .orElseGet(() -> {
             String title = question.length() > 50 ? question.substring(0, 47) + "....." : question;
             return conversationRepository.save(
                     Conversation.builder().id(conversationId)
                             .title(title)
                             .user(user)
                             .createdAt(LocalDateTime.now())
                             .updatedAt(LocalDateTime.now())
                             .build()
             );
         });
	}

	private String buildPrompt(String question, String contextText) {
		 if (contextText != null && !contextText.isBlank()) {
	            return String.format("""
	                           Document Context:
	                           ---------------------
	                           %s
	                           ---------------------
	                           User Message / Question: %s
	                           Instructions:
	                           - If the user's question relates to the document context above, prioritize answering using that context and reference key sections.
	                           - If the user is asking a general question, greeting, or discussing topics beyond the document context, respond helpfully and conversationally using your general knowledge while weaving in relevant document context if applicable

	                    """, contextText, question);
	        } else {
	            return String.format("""
	                       User Message / Question:
	                       %s
	                       Instructions:
	                          - Respond helpfully, accurately, and conversationally to the user's message using your broad knowledge base.

	                    """, question);
	        }
	}

	public Flux<String> streamQuestionAnswer(ChatRequestDto request,User user){
		logger.info("Streaming query: '{}'", request.getQuestion());
        String conversationId = request.getConversationId() != null ? request.getConversationId() : UUID.randomUUID().toString();
        request.setConversationId(conversationId);
        ensureConversationExists(conversationId, user, request.getQuestion());

        List<Document> relevantDocuments = retrieveRelevantDocuments(
                request.getQuestion(),
                request.getDocumentId(),
                request.getTopK(),
                request.getMinSimilarity(),
                user
        );
        String contextText = buildContextString(relevantDocuments);
//        String userPrompt = buildPrompt(requestDto.getQuestion(), contextText);
        return chatClient.prompt()
                .system(s-> s.param("doc_context",contextText))
                .user(request.getQuestion())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.getConversationId()))
                .stream()
                .content()
                .concatWith(Flux.just("[DONE]"));
	}

	private String buildContextString(List<Document> similarDocuments) {
		if(similarDocuments==null || similarDocuments.isEmpty()) {
			return "";
		}
		return similarDocuments.stream().map(doc -> {
			String fileName=(String) doc.getMetadata().getOrDefault("fileName", "Unknown File");
			Object page=doc.getMetadata().getOrDefault("pageNumber", "N/A");
			return String.format("[Source: %s | Page: %s]\n%s", fileName,page,doc.getText());
		}).collect(Collectors.joining("\n\n----\n\n"));
	}
}
