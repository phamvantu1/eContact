package com.ec.contract.model.dto.keystoreDTO;

import com.ec.contract.model.entity.keystoreEntity.Certificate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateDto {
    private Integer id;

    private Integer status;
    private Integer orgAdminCreate;
    private String keyStoreFileName;
    private String certInformation;
    private Date keystoreDateStart;
    private Date keystoreDateEnd;
    private String keystoreSerialNumber;
    private String issuer;
    private Date createAt;
    private List<CertificateCustomersDto> customers;

    public static CertificateDto fromEntity(Certificate certificate) {
        return CertificateDto.builder()
                .id(certificate.getId())
                .createAt(certificate.getCreateAt())
                .status(certificate.getStatus())
                .orgAdminCreate(certificate.getOrgAdminCreate())
                .keyStoreFileName(certificate.getKeyStoreFileName())
                .certInformation(certificate.getCertInformation())
                .keystoreDateEnd(certificate.getKeystoreDateEnd())
                .keystoreDateStart(certificate.getKeystoreDateStart())
                .keystoreSerialNumber(certificate.getKeystoreSerialNumber())
                .issuer(certificate.getIssuer())
                .customers(certificate.getCertificateCustomers()
                        .stream()
                        .map(CertificateCustomersDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
    public static CertificateDto EntityFromDto(Certificate certificate){
        return CertificateDto.builder()
                .id(certificate.getId())
                .createAt(certificate.getCreateAt())
                .status(certificate.getStatus())
                .orgAdminCreate(certificate.getOrgAdminCreate())
                .keyStoreFileName(certificate.getKeyStoreFileName())
                .certInformation(certificate.getCertInformation())
                .keystoreDateEnd(certificate.getKeystoreDateEnd())
                .keystoreDateStart(certificate.getKeystoreDateStart())
                .keystoreSerialNumber(certificate.getKeystoreSerialNumber())
                .issuer(certificate.getIssuer())
                .customers(new ArrayList<>())
                .build();
    }
}
