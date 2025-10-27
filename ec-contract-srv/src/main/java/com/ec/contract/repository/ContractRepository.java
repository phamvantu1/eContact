package com.ec.contract.repository;

import com.ec.contract.model.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Integer> {

    Boolean existsByContractNo(String contractNo);


    @Query(value = "SELECT * FROM contracts c " +
            "WHERE c.created_by = :customerId " +
            "AND c.status = :status " +
            "AND (:textSearch IS NULL OR c.contract_no ILIKE %:textSearch% OR c.name ILIKE %:textSearch%) " +
            "AND (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
            "ORDER BY c.created_at DESC",
            countQuery = "SELECT count(*) FROM contracts c " +
                    "WHERE c.created_by = :customerId " +
                    "AND c.status = :status " +
                    "AND (:textSearch IS NULL OR c.contract_no ILIKE %:textSearch% OR c.name ILIKE %:textSearch%) " +
                    "AND (:fromDate IS NULL  OR c.created_at >= CAST(:fromDate AS timestamp)) " +
                    "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp))",
            nativeQuery = true)
    Page<Contract> findMyContracts(@Param("customerId") Integer customerId,
                                   @Param("status") Integer status,
                                   @Param("textSearch") String textSearch,
                                   @Param("fromDate") String fromDate,
                                   @Param("toDate") String toDate,
                                   Pageable pageable);

    @Query(value = "SELECT distinct c.* from contracts c " +
            "JOIN participants p ON c.id = p.contract_id " +
            "JOIN recipients r ON r.participant_id = p.id " +
            "Where r.email = :email " +
            "AND r.status IN (:listStatus) " +
            "AND (:textSearch IS NULL OR c.contract_no ILIKE %:textSearch% OR c.name ILIKE %:textSearch%) " +
            "AND (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
            "ORDER BY c.created_at DESC",
            countQuery = "SELECT count(*) from contracts c " +
                    "JOIN participants p ON c.id = p.contract_id " +
                    "JOIN recipients r ON r.participant_id = p.id " +
                    "Where r.email = :email " +
                    "AND r.status IN (:listStatus) " +
                    "AND (:textSearch IS NULL OR c.contract_no ILIKE %:textSearch% OR c.name ILIKE %:textSearch%) " +
                    "AND (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
                    "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp))"
            , nativeQuery = true)
    Page<Contract> findMyProcessContracts(@Param("email") String email,
                                          @Param("listStatus") List<Integer> listStatus,
                                          @Param("textSearch") String textSearch,
                                          @Param("fromDate") String fromDate,
                                          @Param("toDate") String toDate,
                                          Pageable pageable);

}
