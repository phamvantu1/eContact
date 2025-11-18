package com.ec.contract.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashBoardStatisticDTO {

    private  Integer totalDraff;

    private  Integer totalCreated;

    private  Integer totalCancel;

    private  Integer totalReject;

    private  Integer totalSigned;

    private  Integer totalLiquidation;

    private  Integer totalProcessing;

    private  Integer totalExpires;

    private  Integer totalAboutExpire;

    private  Integer totalWaiting;

}
