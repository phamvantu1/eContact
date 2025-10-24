package com.ec.contract.model.entity;

import com.ec.library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "participants")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Participant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private Integer type;

    private Integer ordering; // thu tu xu ly

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id") // foreign key trỏ về contracts.id
    private Contract contract;

    private Integer status;

    private String taxCode;
}
