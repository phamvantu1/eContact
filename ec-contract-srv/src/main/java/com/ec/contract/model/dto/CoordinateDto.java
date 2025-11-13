package com.ec.contract.model.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class CoordinateDto {
	private Integer page;
 
    private int boxX;

    private int boxY;

    private int boxW;

    private int boxH;
}
