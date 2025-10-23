package com.ec.contract.model.entity;


import com.ec.library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contract_refs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractRef extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer contractId;

    private Integer refId;
}
