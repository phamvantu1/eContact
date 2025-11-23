package com.ec.contract.repository;

import com.ec.contract.model.entity.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TypeRepository extends JpaRepository<Type, Integer> {


    @Query(value = "SELECT * FROM types t " +
            "where t.status = 1 " +
            "and (:organizationId IS NULL OR t.organization_id = :organizationId) " +
            "and (:name IS NULL OR t.name ILIKE %:name%) " +
            "ORDER BY t.created_at DESC",
            countQuery = "SELECT count(*) FROM types t " +
                    "where t.status = 1 " +
                    "and (:organizationId IS NULL OR t.organization_id = :organizationId) " +
                    "and (:name IS NULL OR t.name ILIKE %:name%) "
            , nativeQuery = true)
    Page<Type> findByNameContainingAndStatus(@Param("name") String name,
                                             @Param("organizationId") Integer organizationId,
                                             Pageable pageable);


    @Query(value = "SELECT * FROM types t " +
            "where t.organization_id = :organizationId " +
            "and t.status = :status ",
            nativeQuery = true)
    List<Type> findByOrganizationIdAndStatus(Integer organizationId, Integer status);
}
