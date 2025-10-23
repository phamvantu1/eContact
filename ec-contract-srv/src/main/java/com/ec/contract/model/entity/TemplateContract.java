package com.ec.contract.model.entity;

import com.ec.library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "template_contracts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemplateContract extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private LocalDateTime startTime;

    private Integer typeId;

    private Integer customerId;

    private Integer status;

    private Integer organizationId;

    private LocalDateTime endTime;
}
