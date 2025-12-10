package com.ec.notification.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @Column(name = "mail_template", columnDefinition = "TEXT")
    private String mailTemplate;

    @Column(name = "notice_template", columnDefinition = "TEXT")
    private String noticeTemplate;
}
