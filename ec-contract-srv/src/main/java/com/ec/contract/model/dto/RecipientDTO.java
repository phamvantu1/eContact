package com.ec.contract.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientDTO implements Serializable, Comparable<RecipientDTO> {

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

    private String cardId;

    private Integer participantId;

    private Set<FieldDto> fields;

    // dieu phoi -> xem xet -> ky -> van thu
    @Override
    public int compareTo(RecipientDTO other) {
        if (role == other.getRole()) {
            return ordering - other.getOrdering();
        }
        return role - other.getRole();
    }

}
