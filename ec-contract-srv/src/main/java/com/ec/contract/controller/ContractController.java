package com.ec.contract.controller;

import com.ec.contract.model.dto.request.ContractRequestDTO;
import com.ec.contract.service.ContractService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Contract API", description = "Quản lý thông tin hợp đồng")
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/check-code-unique")
    @Operation(summary = "Kiểm tra mã hợp đồng có duy nhất hay không", description = "Kiểm tra mã hợp đồng có duy nhất hay không trong hệ thống." +
            " Nếu mã hợp đồng đã tồn tại, trả về false; ngược lại trả về true.")
    public Response<?> checkCodeUnique(@RequestParam(name = "code") String code){
        return Response.success(contractService.checkCodeUnique(code));
    }

    @PostMapping("/create-contract")
    @Operation(summary = "Tạo hợp đồng mới", description = "Tạo một hợp đồng mới trong hệ thống.")
    public Response<?> createContract(@RequestBody ContractRequestDTO contractRequestDTO,
                                      Authentication authentication){
        return Response.success(contractService.createContract(contractRequestDTO, authentication));
    }

}
