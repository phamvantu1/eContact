package com.ec.contract.model.dto.response;

import com.ec.library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponseDTO {

    private Integer id;

    private String name;

    private String path;

    private Integer status;

    private Integer type; // 1 - file goc, 2 - file view cho user , 3 - file dinh kem, 4 - file hop dong theo lô, 5- file hoàn thành được nén lại , 6 - file backup hop dong

    private Integer contractId;

    private String fileName; // Tên file gốc (VD: "HopDongNguyenVanA.pdf")

    private String bucketName; // Bucket MinIO chứa file

}
