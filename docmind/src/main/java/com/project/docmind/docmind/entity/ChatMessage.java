package com.project.docmind.docmind.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Table(name="chat_messages")
@Builder
public class ChatMessage {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "conversation_id", nullable = false)
	    private  Conversation conversation;

	    @Enumerated(EnumType.STRING)
	    @Column(name = "message_type", nullable = false)
	    private MessageType messageType;

	    @Column(columnDefinition = "TEXT", nullable = false)
	    private String content;

	    @Column(nullable = false, updatable = false)
	    private LocalDateTime createdAt;
}
