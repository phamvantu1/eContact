package com.ec.customer.repository;

import com.ec.customer.model.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {


    @Query(value = "select c.* from customers c " +
            "where (:textSearch is null or (c.name ilike %:textSearch% or c.email ilike %:textSearch% or c.phone ilike %:textSearch%)) " +
            "and (:organizationId is null or c.organization_id = :organizationId) " +
            " and c.status != 0 " +
            "order by c.created_at desc "
    ,nativeQuery = true)
    Page<Customer> getAllCustomer(@Param("textSearch") String textSearch,
                                  @Param("organizationId") Integer organizationId,
                                  Pageable pageable);

    Optional<Customer> findByEmail(@Param("email") String email);

    @Query(value = "select c.* from customers c " +
            "where (:textSearch is null or (c.name ilike %:textSearch% or c.email ilike %:textSearch% or c.phone ilike %:textSearch%)) " +
            " and c.status = 1 " +
            "order by c.name asc "
            ,nativeQuery = true)
    List<Customer> suggestListCustomer(@Param("textSearch") String textSearch);
}
