package com.project.docmind.docmind.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Conversations")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Conversation {

	@Id
	private String id;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_id",nullable=false)
	private User user;

	@Column(nullable=false)
	private String title;

	@Column(nullable=false,updatable=false)
	private LocalDateTime createdAt;

	@Column(nullable=false)
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy="conversation", cascade=CascadeType.ALL, orphanRemoval=true)
	private List<ChatMessage> messages=new ArrayList<>();

}
