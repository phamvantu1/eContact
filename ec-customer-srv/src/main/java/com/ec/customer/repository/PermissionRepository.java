package com.ec.customer.repository;

import com.ec.customer.model.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {


    @Query(value = "select p.* from permissions p " +
            " where (:textSearch is null or p.name like %:textSearch%) " +
            " order by p.created_at desc "
            , nativeQuery = true)
    Page<Permission> getAllPermission(Pageable pageable,
                                      @Param("textSearch") String textSearch);
}
