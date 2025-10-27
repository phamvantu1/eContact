package com.ec.contract.model.entity;

import com.ec.library.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "participants")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Participant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private Integer type;

    private Integer ordering; // thu tu xu ly

    private Integer status;

    private String taxCode;

    @Column(name = "contract_id")
    private int contractId;

    @ManyToOne
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    @JsonBackReference
    private Contract contract;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Recipient> recipients = new HashSet<>();

    public void addRecipient(Recipient recipient) {
        if (recipient != null) {
            if (recipients == null) {
                recipients = new HashSet<>();
            }

            recipient.setParticipant(this);
            recipients.add(recipient);
        }
    }

}
