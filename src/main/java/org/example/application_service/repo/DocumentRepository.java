package org.example.application_service.repo;

import org.example.application_service.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // You can add custom queries here if needed
}
