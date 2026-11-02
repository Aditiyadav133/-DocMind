package com.project.docmind.docmind.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.docmind.docmind.entity.ChatMessage;

@Repository
public interface ChatMessageRepository  extends JpaRepository<ChatMessage, Long>{

	List<ChatMessage> findByConversation_IdOrderByCreatedAtAsc(String conversationId);

	@Query(value = "SELECT * FROM chat_messages WHERE conversation_id = :conversationId ORDER BY created_at DESC LIMIT :lastN", nativeQuery = true)
    List<ChatMessage> findLastNMessages(@Param("conversationId") String conversationId, @Param("lastN") int lastN);

    void deleteByConversation_Id(String conversationId);

}
