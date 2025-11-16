package com.ec.contract.repository;

import com.ec.contract.model.entity.TemplateParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface TemplateParticipantRepository extends JpaRepository<TemplateParticipant, Integer> {

    Collection<TemplateParticipant> findByContractIdOrderByOrderingAsc(int contractId);

}
