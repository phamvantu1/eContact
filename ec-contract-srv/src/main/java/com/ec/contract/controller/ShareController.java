package com.ec.contract.controller;

import com.ec.contract.model.dto.ShareListDto;
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
@Tag(name = "Share Controller API", description = "Quản lý chia sẻ hợp đồng")
public class ShareController {

    private final ShareService shareService;

    @PostMapping("")
    @Operation(summary = "Tạo chia sẻ hợp đồng", description = "Tạo mới chia sẻ hợp đồng cho người dùng khác")
    public Response<?> createShare(@RequestBody ShareListDto shareListDto) {
        return Response.success(shareService.createShare(shareListDto));
    }

    @GetMapping("")
    @Operation(summary = "Lấy thông tin danh sách chia sẻ hợp đồng", description = "Lấy danh sách chia sẻ hợp đồng")
    public Response<?> getAllShares(@RequestParam(name = "textSearch", required = false) String textSearch,
                                    @RequestParam(name = "fromDate", required = false) String fromDate,
                                    @RequestParam(name = "toDate", required = false) String toDate,
                                    @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                    @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
                                    Authentication authentication) {
        return Response.success(shareService.getAllSharesContract(textSearch, fromDate, toDate, page, size, authentication));
    }


}
