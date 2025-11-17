package com.ec.contract.repository;

import com.ec.contract.model.entity.Field;
import com.ec.contract.model.entity.TemplateField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TemplateFieldRepository extends JpaRepository<TemplateField,Integer> {

    Collection<TemplateField> findAllByRecipientId(int recipientId);

    List<TemplateField> findByContractId(Integer contractId);

}
