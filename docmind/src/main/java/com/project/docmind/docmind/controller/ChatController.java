package com.project.docmind.docmind.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.docmind.docmind.dto.ApiResponse;
import com.project.docmind.docmind.dto.ChatRequestDto;
import com.project.docmind.docmind.dto.ChatResponseDto;
import com.project.docmind.docmind.dto.SearchRequestDto;
import com.project.docmind.docmind.dto.SearchResultDto;
import com.project.docmind.docmind.entity.User;
import com.project.docmind.docmind.service.RagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(
	name="chat management",
	description="all chat related apis goes here"
		)
@RequiredArgsConstructor
public class ChatController {

	private final RagService ragService;

	@PostMapping("/query")
	@Operation(summary="Ask a question against all documents or a specific document with citations")
	public ResponseEntity<ApiResponse<ChatResponseDto>> askQuestion(@Valid @RequestBody ChatRequestDto request,Authentication authentication){
		User user=(User) authentication.getPrincipal();
		ChatResponseDto chatResponseDto=ragService.askQuestion(request,user);
		return ResponseEntity.ok(ApiResponse.<ChatResponseDto>builder().success(true).message(null).data(chatResponseDto).timestamp(LocalDateTime.now()).build());
	}

	@PostMapping(value = "/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream real-time Q&A answer tokens via Server-Sent Events (SSE)")
	public Flux<String> streamQuestion(@Valid @RequestBody ChatRequestDto chatRequestDto,Authentication authentication){
		User user=(User) authentication.getPrincipal();
		return ragService.streamQuestionAnswer(chatRequestDto,user);
	}

	@PostMapping("/search/similarity")
	@Operation(summary="Perform semantic search on stored document vectors")
	public ResponseEntity<ApiResponse<SearchResultDto>> searchSimilar(@Valid @RequestBody SearchRequestDto searchRequestDto,Authentication authentication){
		User user=(User) authentication.getPrincipal();
		SearchResultDto results=ragService.searchSimilarChunks(searchRequestDto,user);
		return ResponseEntity.ok(ApiResponse.<SearchResultDto>builder().success(true).message(null).data(results).timestamp(LocalDateTime.now()).build());
	}
}
