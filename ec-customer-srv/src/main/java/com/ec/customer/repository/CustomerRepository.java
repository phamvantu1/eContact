package com.ec.customer.repository;

import com.ec.customer.model.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {


    @Query(value = "select c.* from customers c " +
            "where (:textSearch is null or (c.name ilike %:textSearch% or c.email ilike %:textSearch% or c.phone ilike %:textSearch%)) " +
            "and (:organizationId is null or c.organization_id = :organizationId) " +
            "order by c.created_at desc "
    ,nativeQuery = true)
    Page<Customer> getAllCustomer(@Param("textSearch") String textSearch,
                                  @Param("organizationId") Long organizationId,
                                  Pageable pageable);
}
