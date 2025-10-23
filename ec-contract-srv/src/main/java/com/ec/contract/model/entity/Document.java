package com.ec.contract.model.entity;

import com.ec.library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Document extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String path;

    private Integer status;

    private Integer type;

    private Integer contractId;

    private String fileName;

    private String bucketName; // tên bucket lưu trữ

}
