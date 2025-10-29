package com.ec.customer.repository;

import com.ec.customer.common.constant.DefineStatus;
import com.ec.customer.model.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface OrganizationRepository extends JpaRepository<Organization, Integer> {

    @Query(value = "Select o.* from organizations o " +
            " where (:textSearch is null or o.name like %:textSearch%) " +
            " and o.status != 0 " +
            " order by o.created_at desc "
    , nativeQuery = true)
    Page<Organization> getAllOrganizations(@Param("textSearch") String textSearch,
                                           Pageable pageable);

    Optional<Organization> findByIdAndStatus(Integer organizationId, Integer status);

}
