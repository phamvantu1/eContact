package com.ec.contract.repository;

import com.ec.contract.constant.FieldType;
import com.ec.contract.model.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FieldRepository extends JpaRepository<Field, Integer> {

    Collection<Field> findByRecipientId(Integer recipientId);

    Collection<Field> findAllByRecipientId(int recipientId);

    List<Field> findByContractId(Integer contractId);

    Collection<Field> findByContractIdOrderByTypeAsc(int contractId);

    Optional<Field> findFirstByRecipientIdAndType(int recipientId, Integer type);

    @Query(value = """
                select f.*
                from fields f
                join recipients r on r.id = f.recipient_id
                join participants p on p.id = r.participant_id
                join contracts c on p.contract_id = c.id
                where c.id = :contractId
                  and r.email = :email
                limit 1
            """, nativeQuery = true)
    Optional<Field> findByRecipientAndContract(@Param("email") String email,
                                               @Param("contractId") Integer contractId);


}
