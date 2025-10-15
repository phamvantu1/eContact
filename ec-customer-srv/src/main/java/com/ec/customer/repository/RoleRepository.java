package com.ec.customer.repository;

import com.ec.customer.model.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query(value = "select r.* from roles r " +
            " where (:textSeach is null or r.name like %:textSeach%) " +
            " order by r.created_at desc "
            ,nativeQuery = true)
    Page<Role> getAllRoles(@Param("textSeach") String textSearch,
                           Pageable pageable);
}
