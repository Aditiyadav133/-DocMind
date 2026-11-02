package com.project.docmind.docmind.repository;

import org.springframework.stereotype.Repository;

import com.project.docmind.docmind.entity.Conversation;
import com.project.docmind.docmind.entity.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation,String> {

	List<Conversation> findByUserOrderByUpdatedAtDesc(User user);
	Optional<Conversation> findByIdAndUser(String id,User user);
	void deleteByIdAndUser(String id,User user);
}
