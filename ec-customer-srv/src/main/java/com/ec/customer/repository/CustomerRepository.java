package com.ec.customer.repository;

import com.ec.customer.model.entity.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {


    List<Customer> getAllCustomer(@Param("textSearch") String textSearch,
                                  @Param("organizationId") Long organizationId,
                                  Pageable pageable);
}
