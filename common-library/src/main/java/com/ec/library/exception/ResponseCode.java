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

    // ======= Custom Example =======
    USER_NOT_FOUND("ERR_404", "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    EMAIL_NOT_FOUND("ERR_404", "Email không đúng", HttpStatus.NOT_FOUND),
    PASSWORD_INVALID("ERR_404", "Mật khẩu không đúng", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("ERR_409", "Email đã tồn tại", HttpStatus.CONFLICT),
    USER_CAN_NOT_UPDATE("ERR_404", "Bạn không thể cập nhập thông tin người khác", HttpStatus.NOT_FOUND),



    NOTIFICATION_NOT_FOUND("ERR_404", "Không tìm thấy thông báo", HttpStatus.NOT_FOUND),


    ROLE_NOT_FOUND("ERR_404", "Không tìm thấy vai trò", HttpStatus.NOT_FOUND),


    OTP_NOT_FOUND("ERR_404", "OTP không tồn tại", HttpStatus.NOT_FOUND),
    OTP_USED("ERR_409", "OTP đã được sử dụng", HttpStatus.CONFLICT),
    OTP_EXPIRED("ERR_400", "OTP đã hết hạn", HttpStatus.BAD_REQUEST),
    CONFIRM_PASSWORD_NOT_MATCH("ERR_400", "Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST),
    INVALID_OLD_PASSWORD("ERR_400", "Mật khẩu cũ không đúng", HttpStatus.BAD_REQUEST),

    PROJECT_NOT_FOUND("ERR_404", "Không tìm thấy dự án", HttpStatus.NOT_FOUND),
    TASK_NOT_FOUND("ERR_404", "Không tìm thấy công việc", HttpStatus.NOT_FOUND),
    YOU_DONT_PERMISSIT_TASK("404", "Bạn không có quyền thực hiện công việc này" , HttpStatus.FORBIDDEN),

    DEPARTMENT_NOT_FOUND("ERR_404", "Không tìm thấy phòng ban", HttpStatus.NOT_FOUND),
    DEPARTMENT_ALREADY_DELETED("ERR_409", "Phòng ban đã bị xóa", HttpStatus.CONFLICT),
    USER_ALREADY_IN_DEPARTMENT("ERR_409", "Người dùng đã có trong phòng ban", HttpStatus.CONFLICT),
    DEPARTMENT_HAS_LEADER("409", "Đơn vị đã có lãnh đạo", HttpStatus.CONFLICT ),
    USER_NOT_IN_DEPARTMENT("ERR_404", "Người dùng không có trong phòng ban", HttpStatus.NOT_FOUND),

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
