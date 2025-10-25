package com.ec.contract.model.dto.response;

import com.ec.contract.model.entity.Recipient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponseDTO {
    private Integer id;

    private String name;

    private Integer type;

    private Integer ordering; // thu tu xu ly

    private Integer status;

    private String taxCode;

    private Integer contractId;

    private Set<RecipientResponseDTO> recipients;
}
