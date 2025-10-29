package com.ec.contract.model.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ShareDto implements Serializable {
    private Integer id;

    private String email;

    private String password;

    private int status;

    private int contractId;
}
