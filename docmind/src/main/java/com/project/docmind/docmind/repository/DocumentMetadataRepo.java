package com.project.docmind.docmind.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.docmind.docmind.entity.DocumentMetadata;
import com.project.docmind.docmind.entity.DocumentStatus;
import com.project.docmind.docmind.entity.User;

@Repository
public interface DocumentMetadataRepo extends JpaRepository<DocumentMetadata,UUID>{

	List<DocumentMetadata> findAllByOrderByCreatedAtDesc();

	List<DocumentMetadata> findByStatus(DocumentStatus status);

	List<DocumentMetadata> findByUserOrderByCreatedAtDesc(User user);

	Optional<DocumentMetadata> findByIdAndUser(UUID id,User user);
}
