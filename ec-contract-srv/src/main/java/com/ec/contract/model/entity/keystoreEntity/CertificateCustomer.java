package com.ec.contract.model.entity.keystoreEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


import java.io.Serializable;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "certificate_customer")
public class CertificateCustomer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;

    private String phone;

    private int OrganizationId;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToMany(mappedBy = "certificateCustomers",fetch = FetchType.LAZY)
    @Fetch(FetchMode.SELECT)
    private List<Certificate> certificates;
}
