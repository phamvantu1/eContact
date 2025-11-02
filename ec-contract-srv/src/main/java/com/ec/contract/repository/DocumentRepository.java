package com.ec.contract.repository;

import com.ec.contract.model.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Integer> {

    List<Document> findByContractId(Integer contractId);

    @Query(value = "Select * from documents d " +
            "where d.contract_id = :contractId " +
            "and d.type = :type" +
            " limit 1"
    , nativeQuery = true)
    Document findByContractIdAndType(@Param("contractId") Integer contractId,
                                     @Param("type") Integer type);

    List<Document> findByContractIdAndStatus(Integer contractId, Integer status);
}
