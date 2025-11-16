package com.ec.contract.model.entity;

import com.ec.library.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "template_fields")
public class TemplateField extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private Integer type; // 1-text, 2- ky anh , 3 - ky so , 4- ô so hop dong , 5 - ô tiền

    private String value; // gia tri ô

    private Integer page;

    private Double boxX; // toa do x

    private Double boxY; // toa do y

    private Double boxW; //  chiều rộng ô

    private Double boxH; // chiều cao ô

    private Integer documentId;

    private Integer contractId;

    private Integer typeImageSignature = 3;

    private String fieldName;

    private Integer status;

    @Column(name = "recipient_id")
    private Integer recipientId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", insertable = false, updatable = false)
    @ToString.Exclude
    @JsonBackReference
    private TemplateRecipient recipient;
}
