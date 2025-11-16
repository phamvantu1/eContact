package com.ec.contract.model.entity;

import com.ec.library.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "template_participants")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemplateParticipant extends BaseEntity {
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
    private TemplateContract contract;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OrderBy("role asc, ordering asc, id asc")
    private Set<TemplateRecipient> recipients = new HashSet<>();

    public void addRecipient(TemplateRecipient recipient) {
        if (recipient != null) {
            if (recipients == null) {
                recipients = new HashSet<>();
            }

            recipient.setParticipant(this);
            recipients.add(recipient);
        }
    }

}
