package com.ec.contract.controller;

import com.ec.contract.service.DashBoardService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Controller", description = "APIs for managing Dashboard Controller")
public class DashBoardController {

    private final DashBoardService dashBoardService;

    @GetMapping("/my-process")
    @Operation(summary = "Lấy thông tin dashboard hợp đồng của tôi xử lý", description = "Lấy thông tin dashboard hợp đồng của tôi xử lý")
    public Response<?> getMyProcessDashboard(Authentication authentication) {
        return Response.success(dashBoardService.getMyProcessDashboard(authentication));
    }

    @GetMapping("/my-contract")
    @Operation(summary = "Lấy thông tin dashboard hợp đồng của tôi tạo", description = "Lấy thông tin dashboard hợp đồng của tôi tạo")
    public Response<?> getMyContractDashboard(Authentication authentication,
                                              @RequestParam(name = "fromDate", required = false) String fromDate,
                                              @RequestParam(name = "toDate", required = false) String toDate) {
        return Response.success(dashBoardService.getMyContractDashboard(authentication, fromDate, toDate));
    }

    @GetMapping("/count-contract-by-organization")
    @Operation(summary = "Đếm số hợp đồng theo đơn vị", description = "Đếm số hợp đồng theo đơn vị")
    public Response<?> countContractByOrganization(@RequestParam(name = "fromDate", required = false) String fromDate,
                                                   @RequestParam(name = "toDate", required = false) String toDate,
                                                   @RequestParam(name = "organizationId", required = false) Integer organizationId) {
        return Response.success(dashBoardService.countContractByOrganization(fromDate, toDate, organizationId));
    }

    @Operation(summary = "Thống kê khách hàng sử dụng nhiều hợp đồng nhất", description = "Thống kê khách hàng sử dụng nhiều hợp đồng nhất")
    @GetMapping("/statistics/customer-user-max-contracts")
    public Response<?> statisticsCustomerUseMaxContracts() {
        return Response.success(dashBoardService.statisticsCustomerUseMaxContracts());
    }
}
