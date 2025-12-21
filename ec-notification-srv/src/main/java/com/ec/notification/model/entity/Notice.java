package com.ec.notification.model.entity;


import com.ec.library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "notices")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Notice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String contractNo;

    private String noticeContent;

    private String noticeUrl;

    private String email;

    private boolean isRead;

}
