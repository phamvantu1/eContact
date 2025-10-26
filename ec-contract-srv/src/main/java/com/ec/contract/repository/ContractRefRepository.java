package com.ec.contract.repository;

import com.ec.contract.model.entity.ContractRef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRefRepository extends JpaRepository<ContractRef,Integer> {

    List<ContractRef> findByContractId(Integer contractId);
}
