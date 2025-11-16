package com.ec.contract.repository;

import com.ec.contract.model.entity.TemplateRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRecipientRepository extends JpaRepository<TemplateRecipient, Integer> {
}
