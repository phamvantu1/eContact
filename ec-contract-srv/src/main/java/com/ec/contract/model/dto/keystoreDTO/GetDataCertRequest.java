package com.ec.contract.model.dto.keystoreDTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetDataCertRequest {
    public Integer idCert;
    public String[] dataSubject;
}
