package com.ec.contract.model.dto.response;

import lombok.Data;

@Data
public class DashBoardStatisticDTO {

    private final long totalDraff;

    private final long totalCreated;

    private final long totalCancel;

    private final long totalReject;

    private final long totalSigned;

    private final long totalLiquidation;

    private final long totalProcess;

    private final long totalExpires;

    private final long totalAboutExpire;

}
