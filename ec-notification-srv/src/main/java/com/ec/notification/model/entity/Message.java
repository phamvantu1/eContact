package com.ec.notification.model.entity;

import com.ec.library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

@Table(name = "messages")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Message{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Lob
    @Column(name = "mail_template", columnDefinition = "TEXT")
    private String mailTemplate;

    private String url;

    @Lob
    @Column(name = "notice_template", columnDefinition = "TEXT")
    private String noticeTemplate;

    private String code;
}
