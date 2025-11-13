package com.ec.contract.repository;

import com.ec.contract.model.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Integer> {

    Boolean existsByContractNo(String contractNo);

    @Query(value = "SELECT * FROM contracts c " +
            "WHERE c.status = :status " +
            "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
            "AND (:organizationId IS NULL OR c.organization_id = :organizationId ) " +
            "ORDER BY c.created_at DESC",
            countQuery = "SELECT count(*) FROM contracts c " +
                    "WHERE c.status = :status " +
                    "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
                    "AND (:organizationId IS NULL OR c.organization_id = :organizationId )",
            nativeQuery = true)
    Page<Contract> findByStatus(
            @Param("status") Integer status,
            @Param("textSearch") String textSearch,
            @Param("organizationId") Integer organizationId,
            Pageable pageable);



    @Query(value = "SELECT * FROM contracts c " +
            "WHERE c.created_by = :customerId " +
            "AND c.status = :status " +
            "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
            "AND (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
            "ORDER BY c.created_at DESC",
            countQuery = "SELECT count(*) FROM contracts c " +
                    "WHERE c.created_by = :customerId " +
                    "AND c.status = :status " +
                    "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
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
            "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
            "AND (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
            "ORDER BY c.created_at DESC",
            countQuery = "SELECT count(*) from contracts c " +
                    "JOIN participants p ON c.id = p.contract_id " +
                    "JOIN recipients r ON r.participant_id = p.id " +
                    "Where r.email = :email " +
                    "AND r.status IN (:listStatus) " +
                    "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
                    "AND (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
                    "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp))"
            , nativeQuery = true)
    Page<Contract> findMyProcessContracts(@Param("email") String email,
                                          @Param("listStatus") List<Integer> listStatus,
                                          @Param("textSearch") String textSearch,
                                          @Param("fromDate") String fromDate,
                                          @Param("toDate") String toDate,
                                          Pageable pageable);


    @Query(value = "SELECT * FROM contracts c " +
            "WHERE c.created_by in (:listCustomerIds) " +
            "AND c.status = :status " +
            "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
            "AND (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
            "ORDER BY c.created_at DESC",
            countQuery = "SELECT count(*) FROM contracts c " +
                    "WHERE c.created_by = :customerId " +
                    "AND c.status = :status " +
                    "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
                    "AND (:fromDate IS NULL  OR c.created_at >= CAST(:fromDate AS timestamp)) " +
                    "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp))",
            nativeQuery = true)
    Page<Contract> findContractsByOrganization(@Param("listCustomerIds") List<Integer> listCustomerIds,
                                               @Param("status") Integer status,
                                               @Param("textSearch") String textSearch,
                                               @Param("fromDate") String fromDate,
                                               @Param("toDate") String toDate,
                                               Pageable pageable);


    /**
     * Lấy hợp đồng theo recipientId
     * @return
     */
    @Query(
            value = " select c.* from contracts c " +
                    " join participants p on c.id = p.contract_id " +
                    " join recipients r on p.id = r.participant_id " +
                    " where r.id = :recipientId ",
            nativeQuery = true
    )
    Optional<Contract> findByRecipientId(
            @Param("recipientId") Integer recipientId
    );

}
