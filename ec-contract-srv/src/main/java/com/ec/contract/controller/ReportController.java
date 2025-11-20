package com.ec.contract.controller;

import com.ec.contract.service.ReportService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Report Controller", description = "APIs for managing Report Controller")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/detail/{organizationId}")
    @Operation(summary = "Lấy chi tiết báo cáo", description = "Lấy chi tiết báo cáo.")
    public Response<?> reportDetail(@PathVariable("organizationId") int organizationId,
                                    @RequestParam(name = "fromDate")  String fromDate,
                                    @RequestParam(name = "toDate") String toDate,
                                    @RequestParam(name = "completed_from_date", required = false) String completedFromDate,
                                    @RequestParam(name = "completed_to_date", required = false) String completedToDate,
                                    @RequestParam(name = "status", required = false, defaultValue = "-1") Integer status,
                                    @RequestParam(name = "textSearch", required = false, defaultValue = "") String textSearch,
                                    @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                    @RequestParam(name = "size", required = false, defaultValue = "10") Integer size){

        return Response.success(reportService.reportDetail(organizationId, fromDate, toDate, completedFromDate, completedToDate, status, textSearch, page, size));
    }

    @GetMapping("/by-status/{organizationId}")
    @Operation(summary = "Lấy báo cáo theo trạng thái", description = "Lấy báo cáo theo trạng thái.")
    public Response<?> reportByStatus(@PathVariable("organizationId") int organizationId,
                                    @RequestParam(name = "fromDate")  String fromDate,
                                    @RequestParam(name = "toDate") String toDate,
                                    @RequestParam(name = "completed_from_date", required = false) String completedFromDate,
                                    @RequestParam(name = "completed_to_date", required = false) String completedToDate,
                                    @RequestParam(name = "status", required = false, defaultValue = "-1") Integer status,
                                    @RequestParam(name = "textSearch", required = false, defaultValue = "") String textSearch,
                                    @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                    @RequestParam(name = "size", required = false, defaultValue = "10") Integer size){

        return Response.success(reportService.reportByStatus(organizationId, fromDate, toDate, completedFromDate, completedToDate, status, textSearch, page, size));
    }
}

