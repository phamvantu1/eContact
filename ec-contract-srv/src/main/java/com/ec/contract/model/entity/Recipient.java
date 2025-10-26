package com.ec.contract.model.entity;

import com.ec.library.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "recipients")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Recipient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String email;

    private String phone;

    private Integer role; // 1 : dieu phoi , 2: xem xet, 3:  ky, 4 : van thu

    private String username;

    private String password;

    private Integer ordering; // thu tu xu ly

    private Integer status; // 0 : mac dinh, 1 : dang xu ly, 2 : da xu ly, 3 : tu choi, 4 : xac thuc, 5 : cho

    private LocalDateTime fromAt; // ngay nhan

    private LocalDateTime dueAt; // han xu ly

    private LocalDateTime signAt; // ngay ky

    private LocalDateTime processAt; // ngay xu ly

    private Integer signType;

    private String reasonReject;

    private Integer delegateTo; // nguoi duoc uy quyen

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @OneToMany(mappedBy = "recipient", fetch = FetchType.LAZY)
    private Set<Field> fields;

}
