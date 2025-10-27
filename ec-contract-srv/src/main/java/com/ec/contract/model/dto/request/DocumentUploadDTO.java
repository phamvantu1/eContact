package com.ec.contract.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadDTO {

    private String name;

    private Integer type;

    private Integer contractId;

    private String fileName;

    private String path;

    private Integer status;

}
