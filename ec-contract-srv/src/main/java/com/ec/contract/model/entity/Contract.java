package com.ec.contract.model.entity;

import com.ec.library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Contract extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String contractNo;

    private LocalDateTime signTime;

    private String note;

    private Integer refId; // hop dong lien quan

    private Integer typeId; // loai hop dong

    private Integer customerId; // id nguoi tao

    private Boolean isTemplate; // la hop dong mau hay khong

    private Integer status;

    private Integer organizationId; // to chuc

    private String reasonReject; // ly do tu choi hop dong

    private Integer templateContractId;

    private LocalDateTime contractExpireTime; // ngay het han hop dong
}
