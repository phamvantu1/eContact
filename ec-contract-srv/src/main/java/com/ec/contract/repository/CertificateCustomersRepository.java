package com.ec.contract.repository;

import com.ec.contract.model.entity.keystoreEntity.CertificateCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CertificateCustomersRepository extends JpaRepository<CertificateCustomer, Long> {
    Optional<CertificateCustomer> findFirstByEmail(String email);

    Optional<CertificateCustomer> findFirstByPhone(String phone);


    @Query(value = "SELECT * FROM certificate_customer c WHERE " +
            " (:loginType = 'EMAIL' AND c.email = :email) OR " +
            " (:loginType = 'SDT' AND c.phone = :phone) " +
//            " (:loginType = 'EMAIL_AND_SDT' and ( c.email = :email OR c.phone = :phone)) " +
            " LIMIT 1 ", nativeQuery = true)
    Optional<CertificateCustomer> findByPhoneOrEmailAndLoginType(
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("loginType") String loginType);

    @Query(value = "SELECT * FROM certificate_customer c WHERE " +
            " (:loginType = 'EMAIL_AND_SDT' and ( c.email = :email OR c.phone = :phone)) "
            , nativeQuery = true)
    List<CertificateCustomer> findByPhoneOrEmailAndLoginTypeByEmailAndSDT(
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("loginType") String loginType);
}
