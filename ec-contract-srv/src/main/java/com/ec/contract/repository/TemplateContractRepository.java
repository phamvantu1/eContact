package com.ec.contract.repository;

import com.ec.contract.constant.BaseStatus;
import com.ec.contract.model.entity.TemplateContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TemplateContractRepository extends JpaRepository<TemplateContract, Integer> {

    Boolean existsByContractNo(String contractNo);

    @Query(value = "SELECT c.* " +
            "from template_contracts c " +
            " where c.customer_id = :customerId " +
            " and c.status != 0 " +
            " and (:type is null or c.type_id = :type ) " +
            " and (:name is null or c.name like %:name% ) " +
            " order by c.created_at desc "
    , nativeQuery = true)
    Page<TemplateContract> getMyTemplateContracts(@Param("customerId") Integer customerId,
                                                 @Param("type") Integer type,
                                                 @Param("name") String name,
                                                 Pageable pageable);

}
