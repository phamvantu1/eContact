package com.ec.contract.model.dto;

import com.ec.contract.constant.RecipientRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
public class BpmnRecipientDto implements Comparable<BpmnRecipientDto> {
    private Integer id;

    private String name;

    private String email;

    private String phone;

    private Integer role;

    private String username;

    private Integer ordering;

    private Integer status;

    private String cardId;

    private String reasonReject;

    private LocalDateTime processAt;

    private Integer signType;

    @JsonIgnore
    private Integer participantOrder;

    private String participantName;

    private Integer participantType;

    @JsonIgnore
    private ParticipantDTO participant;

    private String userInOrganization;

    @Override
    public int compareTo(BpmnRecipientDto other) {

        // 1️⃣ Người điều phối (COORDINATOR) luôn đứng đầu danh sách
        if (role == RecipientRole.COORDINATOR.getDbVal() && other.getRole() != role) {
          return -1;
        }

        // 2️⃣ Nếu participantOrder khác nhau → sắp theo participantOrder tăng dần
        if (participantOrder != other.getParticipantOrder()) {
            return participantOrder - other.getParticipantOrder();
        }

        // 3️⃣ Nếu cùng participantOrder nhưng role khác → sắp theo role tăng dần
        if (role != other.getRole()) {
            return role - other.getRole();
        }

        // 4️⃣ Cuối cùng, nếu tất cả giống nhau → sắp theo ordering tăng dần
        return ordering - other.getOrdering();
    }

    private boolean isRecipientHistory;

}
