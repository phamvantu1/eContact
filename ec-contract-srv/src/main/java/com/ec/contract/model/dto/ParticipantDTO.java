package com.ec.contract.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDTO implements Comparable<ParticipantDTO>{
    private Integer id;

    private String name;

    private Integer type; // 1 - to chuc cua toi , 2 - to chuc doi tac , 3 - ca nhan

    private Integer ordering; // thu tu xu ly

    private Integer status;

    private String taxCode;

    private Integer contractId;

    private Set<RecipientDTO> recipients;

    public boolean isSame(ParticipantDTO other) {
        return Objects.equals(this.id, other.id) &&
                Objects.equals(this.name, other.name) &&
                this.type == other.type &&
                Objects.equals(this.taxCode, other.taxCode);
    }

    @Override
    public int compareTo(ParticipantDTO other) {
        if (type == 1) {
            return -1;
        } else if (other.getType() == 1) {
            return 1;
        }

        if (ordering== other.getOrdering()) {
            return id - other.getId();
        }

        return ordering - other.ordering;
    }

}
