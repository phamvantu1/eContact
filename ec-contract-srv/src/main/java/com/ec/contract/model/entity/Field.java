package com.ec.contract.model.entity;

import com.ec.library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fields")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Field extends BaseEntity {
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
}
