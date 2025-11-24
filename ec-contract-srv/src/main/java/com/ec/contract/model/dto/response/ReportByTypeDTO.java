package com.ec.contract.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportByTypeDTO {

    private String typeName;

    private DashBoardStatisticDTO statistic;
}
