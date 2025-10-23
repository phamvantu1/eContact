package com.ec.contract.repository;

import com.ec.contract.model.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeRepository extends JpaRepository<Integer, Type> {
}
