package com.ec.contract.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipients")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Recipient {

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

    private Integer signTye;

    private Integer participantId;

    private String reasonReject;

    private Integer delegateTo; // nguoi duoc uy quyen


}
