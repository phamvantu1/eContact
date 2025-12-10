package com.ec.notification.model.entity;

import com.ec.library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "emails")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Email extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String subject;

    private String recipient;

    private String cc;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer status;

}
