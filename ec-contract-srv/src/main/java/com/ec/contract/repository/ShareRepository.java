package com.ec.contract.repository;

import com.ec.contract.model.entity.Contract;
import com.ec.contract.model.entity.Share;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShareRepository extends JpaRepository<Share, Integer> {

    Optional<Share> findFirstByContractIdAndEmail(Integer contractId, String email);


    @Query(value =
            "SELECT DISTINCT c.* FROM contracts c " +
                    "JOIN shares s ON c.id = s.contract_id " +
                    "WHERE s.email = :email " +
                    "AND (:textSearch IS NULL OR (" +
                    "     c.contract_no LIKE CONCAT('%', :textSearch, '%') " +
                    "     OR c.name LIKE CONCAT('%', :textSearch, '%')" +
                    ")) " +
                    "AND (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
                    "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
                    "ORDER BY c.created_at DESC",
            nativeQuery = true)
    Page<Contract> getAllSharesContract(@Param("email") String email,
                                        @Param("textSearch") String textSearch,
                                        @Param("fromDate") String fromDate,
                                        @Param("toDate") String toDate,
                                        Pageable pageable);
}
