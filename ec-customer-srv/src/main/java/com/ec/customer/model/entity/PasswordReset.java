package com.ec.customer.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "password_resets")
@Data
public class PasswordReset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

}
