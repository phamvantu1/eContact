package com.ec.contract.controller;

import com.ec.contract.service.DashBoardService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Controller", description = "APIs for managing Dashboard Controller")
public class DashBoardController {

    private final DashBoardService dashBoardService;

    @GetMapping("/my-process")
    @Operation(summary = "Lấy thông tin dashboard hợp đồng của tôi xử lý", description = "Lấy thông tin dashboard hợp đồng của tôi xử lý")
    public Response<?> getMyProcessDashboard() {
        return Response.success(dashBoardService.getMyProcessDashboard());
    }
}
