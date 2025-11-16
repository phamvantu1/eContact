package com.ec.contract.model.entity;

import com.ec.library.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "template_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class TemplateContract extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String contractNo;

    private LocalDateTime signTime;

    private String note;

    private Integer typeId; // loai hop dong

    private Integer customerId; // id nguoi tao

    private Boolean isTemplate; // la hop dong mau hay khong

    private Integer status;

    private Integer organizationId; // to chuc

    private String reasonReject; // ly do tu choi hop dong

    private LocalDateTime cancelDate;

    private Integer templateContractId;

    private LocalDateTime contractExpireTime; // ngay het han hop dong

    @JsonIgnore
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("ordering asc")
    @JsonManagedReference
    private Set<TemplateParticipant> participants;
}
