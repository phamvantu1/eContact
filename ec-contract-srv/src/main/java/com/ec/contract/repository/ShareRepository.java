package com.ec.contract.repository;

import com.ec.contract.model.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareRepository extends JpaRepository<Share, Integer> {

    Optional<Share> findFirstByContractIdAndEmail(Integer contractId, String email);
}
