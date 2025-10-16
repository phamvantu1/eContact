package com.ec.notification.model.entity;

import com.ec.library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name = "email")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class email extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String subject;

    private String recipient;

    private String cc;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer status;

}
