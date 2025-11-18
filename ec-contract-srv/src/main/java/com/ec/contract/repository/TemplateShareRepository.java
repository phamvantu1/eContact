package com.ec.contract.repository;

import com.ec.contract.model.entity.TemplateContract;
import com.ec.contract.model.entity.TemplateShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TemplateShareRepository extends JpaRepository<TemplateShare, Integer> {

    Optional<TemplateShare> findFirstByContractIdAndEmail(Integer contractId, String email);

    @Query(value = "SELECT distinct c.* from template_contracts c  " +
            "join template_shares s on c.id = s.contract_id " +
            "where s.email = :email " +
            "and (:textSearch is null or (c.contract_no like %:textSearch% or c.name like %:textSearch%)) " +
            "AND (:fromDate IS NULL  OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp))" +
            "order by c.created_at desc"
            , nativeQuery = true)
    Page<TemplateContract> getAllSharesContract(@Param("email") String email,
                                                @Param("textSearch") String textSearch,
                                                @Param("fromDate") String fromDate,
                                                @Param("toDate") String toDate,
                                                Pageable pageable);

}
