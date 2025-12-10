package com.ec.contract.repository;

import com.ec.contract.model.entity.Document;
import com.ec.contract.model.entity.TemplateDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TemplateDocumentRepository extends JpaRepository<TemplateDocument, Integer> {

    List<TemplateDocument> findByContractIdAndStatus(Integer contractId, Integer status);

    @Query(
            value = "select d.* from template_documents d" +
                    " where (d.contract_id = :contractId)" +
                    "   and (d.status = :status) " +
                    "order by d.id desc",
            nativeQuery = true)
    Collection<TemplateDocument> findAllByContractIdAndStatusOrderByIdDesc(@Param("contractId") int contractId,
                                                                   @Param("status") int status);


}
