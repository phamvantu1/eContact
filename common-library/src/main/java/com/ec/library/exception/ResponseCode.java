package com.ec.library.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {

    // ======= System Errors =======
    SYSTEM("ERR_501", "System error. Please try again later!", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_SERVER_ERROR("ERR_500", "Lỗi hệ thống , vui lòng đợi trong giây lát", HttpStatus.INTERNAL_SERVER_ERROR),
    NO_CODE("ERR_000", "No error code specified", HttpStatus.INTERNAL_SERVER_ERROR),
    CACHE_FAILED("VAL_500", "Cache failed", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND("ERR_404", "Resource not found", HttpStatus.NOT_FOUND),
    NO_BODY("ERR_400", "No body in request", HttpStatus.BAD_REQUEST),

    // ======= Auth Errors =======
    UNAUTHORIZED("ERR_401", "Authentication failed", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("ERR_403", "Access denied", HttpStatus.FORBIDDEN),

    OTP_NOT_FOUND("ERR_404", "OTP không tồn tại", HttpStatus.NOT_FOUND),
    OTP_USED("ERR_409", "OTP đã được sử dụng", HttpStatus.CONFLICT),
    OTP_EXPIRED("ERR_400", "OTP đã hết hạn", HttpStatus.BAD_REQUEST),
    CONFIRM_PASSWORD_NOT_MATCH("ERR_400", "Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST),
    INVALID_OLD_PASSWORD("ERR_400", "Mật khẩu cũ không đúng", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID("ERR_400", "Mật khẩu không hợp lệ", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID("ERR_400", "Email không hợp lệ", HttpStatus.NOT_FOUND),
    EMAIL_EXISTED("ERR_409", "Email đã tồn tại", HttpStatus.CONFLICT),


    // customer
    CUSTOMER_NOT_FOUND("ERR_404", "Không tìm thấy khách hàng", HttpStatus.NOT_FOUND),
    CUSTOMER_ALREADY_EXISTS("ERR_409", "Khách hàng đã tồn tại", HttpStatus.CONFLICT),
    CUSTOMER_EMAIL_EXISTED("ERR_409", "Email đã tồn tại", HttpStatus.CONFLICT),


    // organization
    ORGANIZATION_NOT_FOUND("ERR_404", "Không tìm thấy tổ chức", HttpStatus.NOT_FOUND),


    // role
    ROLE_ALREADY_EXISTS("ERR_409", "Vai trò đã tồn tại", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND("ERR_404", "Không tìm thấy vai trò", HttpStatus.NOT_FOUND),


    // permission
    PERMISSION_NOT_FOUND("ERR_404", "Không tìm thấy quyền", HttpStatus.NOT_FOUND),


    BAD_REQUEST("ERR_400", "Bad request", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ResponseCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
