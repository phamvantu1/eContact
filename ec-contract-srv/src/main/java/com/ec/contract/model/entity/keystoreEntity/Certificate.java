package com.ec.contract.model.entity.keystoreEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "certificate")
public class Certificate implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String status;

    private Integer orgAdminCreate;

    private String subject;

    @Lob
    private byte[] keystore;

    private String passwordKeystore;

    private String alias;

    private String certInformation;

    private Date keystoreDateStart;

    private Date keystoreDateEnd;

    private String keystoreSerialNumber;

    private String keyStoreFileName;

    private String issuer;

    private Date createAt;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "certificate_mapping",
            joinColumns = @JoinColumn(name = "certificate_id"),
            inverseJoinColumns = @JoinColumn(name = "customer_id"))
    private List<CertificateCustomer> certificateCustomers;
}
