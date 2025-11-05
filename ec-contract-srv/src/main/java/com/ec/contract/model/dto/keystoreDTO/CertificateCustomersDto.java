package com.ec.contract.model.dto.keystoreDTO;

import com.ec.contract.model.entity.keystoreEntity.CertificateCustomer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateCustomersDto {
    private Integer id;
    private String email;
    private String phone;
    private int organizationId;
    private List<CertificateDto> certificates;

    public static CertificateCustomersDto fromEntity(CertificateCustomer customer) {
        return CertificateCustomersDto.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .organizationId(customer.getOrganizationId())
                .certificates(new ArrayList<>())
                .build();
    }

    public static CertificateCustomersDto EntityToDto(CertificateCustomer customer) {
        return CertificateCustomersDto.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .organizationId(customer.getOrganizationId())
                .certificates(customer.getCertificates().stream().map(CertificateDto::EntityFromDto).collect(Collectors.toList()))
                .build();
    }
}
