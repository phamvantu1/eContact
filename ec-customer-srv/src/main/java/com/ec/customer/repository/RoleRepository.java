package com.ec.customer.repository;

import com.ec.customer.model.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query(value = "select r.* from roles r " +
            " where (:textSeach is null or r.name like %:textSeach%) " +
            " and  r.status != 0 " +
            " order by r.created_at desc "
            ,nativeQuery = true)
    Page<Role> getAllRoles(@Param("textSeach") String textSearch,
                           Pageable pageable);

    @Query(value = "select * from  roles r " +
            "where r.name = :name "
            , nativeQuery = true )
    Optional<Role> findByName(@Param("name") String name);
}
