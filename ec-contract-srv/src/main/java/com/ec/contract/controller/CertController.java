package com.ec.contract.controller;

import com.ec.contract.model.dto.keystoreDTO.GetDataCertRequest;
import com.ec.contract.service.CertService;
import com.ec.library.exception.ResponseCode;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;


@RestController
@RequestMapping("/certs")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Cert Controller API", description = "Quản lý chứng thư số")
public class CertController {

    private final CertService certService;

    @PostMapping("/import-cert")
    @Operation(summary = "Import chứng thư số", description = "Import chứng thư số từ file .p12 lên hệ thống")
    public Response<?> importCert(Authentication authentication,
                                  @RequestParam(name = "file") MultipartFile file,
                                  @RequestParam(name = "list_email", required = false) String[] emails,
                                  @RequestParam(name = "password") String password,
                                  @RequestParam(name = "status") Integer status) {
        try {
            String[] fileNameSplit = file.getOriginalFilename().split("\\.");
            if (!(fileNameSplit[fileNameSplit.length - 1].equals("p12"))) {
                return Response.error(ResponseCode.FILE_DONT_TYPE_P12);
            }
            var result = certService.importCertToDatabase(file, emails, password, status, authentication);

            return Response.success(result);
        } catch (RuntimeException e) {
            log.error("Import cert error: ", e);
            return Response.error(ResponseCode.CREATE_CERT_FAILED);
        }
    }

    @GetMapping("/find-cert-user")
    @Operation(summary = "Lấy chứng thư số theo user", description = "Lấy chứng thư số theo user đăng nhập")
    public Response<?> findCertCustomer(Authentication authentication) {
        return Response.success(certService.findCertByUser(authentication));
    }

    @PostMapping("/update-user-from-cert")
    @Operation(summary = "Cập nhật thông tin user từ chứng thư số", description = "Cập nhật thông tin user từ chứng thư số")
    public Response<?> addUserFromCert(Authentication authentication,
                                       @RequestParam(name = "certificateId") Integer certificateId,
                                       @RequestParam(name = "status") Integer status,
                                       @RequestParam(name = "list_email", required = false) String[] emails) {

        var result = certService.updateUserFromCert(certificateId, emails, status, authentication);
        return Response.success(result);
    }

    @DeleteMapping("/remove-user-from-cert")
    @Operation(summary = "Xóa user khỏi chứng thư số", description = "Xóa user khỏi chứng thư số")
    public Response<?> removeUserFromCert(Authentication authentication,
                                          @RequestParam(name = "certificateId") Integer certificateId,
                                          @RequestParam(name = "customerIds") Integer[] customerIds) {
        var result = certService.removeUserFromCert(certificateId, customerIds, authentication);
        return Response.success(result);
    }

    @GetMapping("/cert-information")
    @Operation(summary = "Lấy thông tin chứng thư số", description = "Lấy thông tin chứng thư số theo ID")
    public Response<?> certInformation(@RequestParam(name = "certificateId") Integer certificateId) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {

        var result = certService.getDataCert(new GetDataCertRequest(certificateId, null));
        return Response.success(result);
    }

    @GetMapping("/find-cert-by-id")
    @Operation(summary = "Lấy chứng thư số theo ID", description = "Lấy chứng thư số theo ID")
    public Response<?> findCertById(Authentication authentication,
                                    @RequestParam(name = "certificateId") Integer certificateId) {
        var result = certService.findCertById(certificateId, authentication);
        return Response.success(result);
    }

    @GetMapping("/find-cert")
    @Operation(summary = "Tìm kiếm chứng thư số", description = "Tìm kiếm chứng thư số với các tiêu chí lọc và phân trang")
    public Response<?> findAllCert(Authentication authentication,
                                   @RequestParam(name = "subject", defaultValue = "", required = false) String subject,
                                   @RequestParam(name = "serial_number", defaultValue = "", required = false) String serial_number,
                                   @RequestParam(name = "status", defaultValue = "1", required = false) Integer status,
                                   @RequestParam(name = "size", defaultValue = "10") int size,
                                   @RequestParam(name = "page", defaultValue = "0") int page) {

        var result = certService.findAllCertKeystore(subject, serial_number, status, size, page, authentication);
        return Response.success(result);
    }


}
