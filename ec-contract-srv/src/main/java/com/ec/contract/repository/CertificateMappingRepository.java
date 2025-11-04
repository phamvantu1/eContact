package com.ec.contract.repository;


import com.ec.contract.model.entity.keystoreEntity.Certificate;
import com.ec.contract.model.entity.keystoreEntity.CertificateCustomer;
import com.ec.contract.model.entity.keystoreEntity.CertificateMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateMappingRepository extends JpaRepository<CertificateMapping, Long> {
    void deleteCertificateMappingByCertificateAndCertificateCustomer(Certificate certificate, CertificateCustomer customer);
}
