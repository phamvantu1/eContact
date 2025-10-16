package com.ec.auth.model.entity;


import com.ec.library.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Customer extends BaseEntity {

    private Integer id;

    private String name;

    private String email;

    private String password;

    private String phone;

    private String birthday;

    private String status;

    private String gender;

    private List<String> roles;

    private List<String> permissions;

}
