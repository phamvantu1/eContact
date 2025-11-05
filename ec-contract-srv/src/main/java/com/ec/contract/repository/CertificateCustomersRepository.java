package com.ec.contract.repository;

import com.ec.contract.model.entity.keystoreEntity.CertificateCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CertificateCustomersRepository extends JpaRepository<CertificateCustomer, Integer> {

    Optional<CertificateCustomer> findFirstByEmail(String email);

    Optional<CertificateCustomer> findFirstByPhone(String phone);


    @Query(value = "SELECT * FROM certificate_customer c WHERE " +
            " (c.email = :email)  " +
            " LIMIT 1 ", nativeQuery = true)
    Optional<CertificateCustomer> findByPhoneOrEmailAndLoginType(
            @Param("email") String email);

    @Query(value = "SELECT * FROM certificate_customer c WHERE " +
            " ( c.email = :email ) "
            , nativeQuery = true)
    List<CertificateCustomer> findByPhoneOrEmailAndLoginTypeByEmailAndSDT(
            @Param("email") String email);
}
