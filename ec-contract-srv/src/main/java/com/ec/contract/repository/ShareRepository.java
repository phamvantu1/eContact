package com.ec.contract.repository;

import com.ec.contract.model.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareRepository extends JpaRepository<Share, Integer> {
}
