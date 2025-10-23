package com.ec.contract.repository;

import com.ec.contract.model.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Integer, Document> {
}
