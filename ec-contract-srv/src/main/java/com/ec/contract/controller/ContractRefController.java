package com.ec.contract.controller;

import com.ec.contract.service.ContractRefService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contract-refs")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Contract Ref Controller API", description = "Quản lý thông tin tham chiếu hợp đồng")
public class ContractRefController {

    private final ContractRefService contractRefService;

    @GetMapping("/all-refs")
    @Operation(summary = "Lấy tất cả tham chiếu hợp đồng", description = "Lấy tất cả tham chiếu hợp đồng trong hệ thống.")
    public Response<?> getAllContractRefs(@RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                          @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
                                          @RequestParam(name = "textSearch", required = false) String textSearch,
                                          @RequestParam(name = "organizationId", required = false) Integer organizationId) {
        return Response.success(contractRefService.getAllContractRefs(page, size, textSearch, organizationId));
    }
}
