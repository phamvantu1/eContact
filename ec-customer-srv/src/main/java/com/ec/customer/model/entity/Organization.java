package com.ec.customer.model.entity;

import com.ec.library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "organizations")
@Data
public class Organization extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private Integer status;

    private Integer parentId;

    private String taxCode;

}
