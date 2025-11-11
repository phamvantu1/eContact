package com.ec.contract.repository;

import com.ec.contract.constant.FieldType;
import com.ec.contract.model.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FieldRepository extends JpaRepository<Field, Integer> {

    Collection<Field> findByRecipientId(Integer recipientId);

    Collection<Field> findAllByRecipientId(int recipientId);

    List<Field> findByContractId(Integer contractId);

    Optional<Field> findFirstByRecipientIdAndType(int recipientId, Integer type);

}
