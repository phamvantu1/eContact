package com.ec.contract.repository;

import com.ec.contract.model.entity.keystoreEntity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface CertificateRepository extends JpaRepository<Certificate, Integer> {
    Optional<Certificate> findFirstByKeyStoreFileNameAndOrgAdminCreate(String fileName, int OrgId);

    @Query(value = "select DISTINCT c.* from certificate c " +
            "inner join certificate_mapping cm on c.id = cm.certificate_id " +
            "inner join certificate_customer cc on cm.customer_id = cc.id " +
            "where cc.organization_id = :organizationId " +
            "and (cast(:subject as varchar) is null or (c.subject ilike concat('%', cast(:subject as varchar), '%'))) " +
            "and (cast(:serialNumber as varchar) is null or (c.keystore_serial_number ilike concat('%', cast(:serialNumber as varchar), '%')))" +
            "and (c.status = :status) "
            , nativeQuery = true)
    List<Certificate> findByKeyStoreData(@Param("subject") String subject,
                                         @Param("status") Integer status,
                                         @Param("serialNumber") String serialNumber,
                                         @Param("organizationId") Integer organizationId);

    @Query(value = "select DISTINCT c.id,c.password_keystore from certificate c " +
            "inner join certificate_mapping cm on c.id = cm.certificate_id " +
            "inner join certificate_customer cc on cc.id = cm.customer_id " +
            "where cc.email =:email and c.status =:status order by c.id desc"
            , nativeQuery = true)
    List<Object[]> findByCertificateCustomersEmail(@Param("email") String email,
                                                   @Param("status") Integer status);

}
