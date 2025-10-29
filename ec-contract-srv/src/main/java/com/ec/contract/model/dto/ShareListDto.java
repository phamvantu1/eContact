package com.ec.contract.model.dto;

import lombok.Data;

import java.util.Set;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
public class ShareListDto {

    private Set<String> email;

    private int contractId;
}
