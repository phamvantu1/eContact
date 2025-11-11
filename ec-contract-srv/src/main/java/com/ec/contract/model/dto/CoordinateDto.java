package com.ec.contract.model.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class CoordinateDto {
	private Integer page;
 
    private int coordinateX;

    private int coordinateY;

    private int width;

    private int height;
}
