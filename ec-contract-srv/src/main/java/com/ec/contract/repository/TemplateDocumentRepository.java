package com.ec.contract.repository;

import com.ec.contract.model.entity.TemplateDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateDocumentRepository extends JpaRepository<TemplateDocument, Integer> {

    List<TemplateDocument> findByContractIdAndStatus(Integer contractId, Integer status);


}
