package com.ec.contract.repository;

import com.ec.contract.model.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Integer> {

    List<Document> findByContractId(Integer contractId);

    List<Document> findByContractIdAndStatus(Integer contractId, Integer status);
}
