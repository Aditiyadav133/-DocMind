package com.project.docmind.docmind.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponseDto {

	private String answer;
    private String conversationId;
    private List<CitationDto> citations;
    private Long responseTimeMs;

}
