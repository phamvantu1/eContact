package com.ec.contract.controller;

import com.ec.contract.model.dto.ShareListDto;
import com.ec.contract.model.dto.request.FilterContractDTO;
import com.ec.contract.service.TemplateShareService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/template-share")
@RequiredArgsConstructor
@Tag(name = "Template Share Controller", description = "APIs for managing contract Template Share")
public class TemplateShareController {

    private final TemplateShareService templateShareService;

    @PostMapping("")
    @Operation(summary = "Tạo chia sẻ hợp đồng mẫu ", description = "Tạo mới chia sẻ hợp đồng mẫu cho người dùng khác")
    public Response<?> createShare(@RequestBody ShareListDto shareListDto){
        return Response.success(templateShareService.createShare(shareListDto));
    }

    @GetMapping("")
    @Operation(summary = "Lấy thông tin danh sách chia sẻ hợp đồng mẫu ", description = "Lấy danh sách chia sẻ hợp đồng mẫu")
    public Response<?> getAllShares(@RequestBody FilterContractDTO filterContractDTO,
                                    Authentication authentication){
        return Response.success(templateShareService.getAllSharesContract(filterContractDTO, authentication));
    }
}
