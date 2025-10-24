package com.ec.contract.repository;

import com.ec.contract.model.entity.ContractRef;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRefRepository extends JpaRepository<ContractRef,Integer> {
}
