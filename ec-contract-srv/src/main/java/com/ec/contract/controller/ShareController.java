package com.ec.contract.controller;

import com.ec.contract.model.dto.ShareListDto;
import com.ec.contract.model.dto.request.FilterContractDTO;
import com.ec.contract.service.ShareService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shares")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Share API", description = "Quản lý chia sẻ hợp đồng")
public class ShareController {

    private final ShareService shareService;

    @PostMapping("")
    @Operation(summary = "Tạo chia sẻ hợp đồng", description = "Tạo mới chia sẻ hợp đồng cho người dùng khác")
    public Response<?> createShare(@RequestBody ShareListDto shareListDto){
        return Response.success(shareService.createShare(shareListDto));
    }

    @GetMapping("")
    @Operation(summary = "Lấy thông tin danh sách chia sẻ hợp đồng", description = "Lấy danh sách chia sẻ hợp đồng")
    public Response<?> getAllShares(@RequestBody FilterContractDTO filterContractDTO,
                                    Authentication authentication){
        return Response.success(shareService.getAllSharesContract(filterContractDTO, authentication));
    }


}
