package com.ec.contract.repository;

import com.ec.contract.model.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Integer> {

    Collection<Participant> findByContractIdOrderByOrderingAsc(int contractId);

}
