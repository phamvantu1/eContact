package com.ec.contract.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertResponse {

    private String name;

    private String company;

    // chức vụ
    private String position;

    // mã số thuế
    private String mst;

    // căn cước công dân
    private String cccd;

    // chứng minh nhân dân
    private String cmnd;

    private String dataCert;

    private String serialNumber;

    // Đơn vị cấp
    private String issuer;

    // Thời gian hiệu lực
    private String validFrom;

    private String validTo;

    public static CertResponse certResponseEmpty() {
        return CertResponse.builder()
                .name("")
                .company("")
                .position("")
                .mst("")
                .cccd("")
                .cmnd("")
                .dataCert("")
                .serialNumber("")
                .issuer("")
                .validFrom("")
                .validTo("")
                .build();
    }

}
