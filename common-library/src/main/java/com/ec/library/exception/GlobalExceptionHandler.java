package com.ec.library.exception;

import com.ec.library.response.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 403 - Access denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response<Void>> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(ResponseCode.FORBIDDEN);
    }

    // 401 - Authentication failed
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Response<Void>> handleAuthException(AuthenticationException ex, WebRequest request) {
        return buildErrorResponse(ResponseCode.UNAUTHORIZED);
    }
    // ✅ Lỗi validate @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        // Lấy lỗi đầu tiên
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = (fieldError != null) ? fieldError.getDefaultMessage() : "Dữ liệu không hợp lệ";
        return new ResponseEntity<>(Response.error("VALIDATION_ERROR", message), HttpStatus.BAD_REQUEST);
    }

    // ✅ Lỗi IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(Response.error("VALIDATION_ERROR", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }


    // Custom exception
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Response<Void>> handleCustomException(CustomException ex, WebRequest request) {
        return buildErrorResponse(ex.getResponseCode());
    }

    @ExceptionHandler(CustomerApiException.class)
    public ResponseEntity<Response<Void>> handleCustomerApiException(CustomerApiException ex, WebRequest request) {
        String customerMessage = "Lỗi từ Customer API";

        try {
            // Parse JSON responseBody
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(ex.getResponseBody());
            if (node.has("message")) {
                customerMessage = node.get("message").asText(); // chỉ lấy "Không tìm thấy vai trò"
            }
        } catch (Exception e) {
            // Nếu parse lỗi thì giữ nguyên responseBody thô
            customerMessage = ex.getResponseBody();
        }

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Response.error(String.valueOf(ex.getStatusCode()), customerMessage));
    }



    // 404 - Not found
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Response<Void>> handleNotFound(NoHandlerFoundException ex, WebRequest request) {
        return new ResponseEntity<>(Response.error(ResponseCode.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Response<Void>> handleNoResourceFound(NoResourceFoundException ex, WebRequest request) {
        return new ResponseEntity<>(Response.error(ResponseCode.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    // 400 - Bad request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<Void>> handleNoBody(HttpMessageNotReadableException ex, WebRequest request) {
        return new ResponseEntity<>(Response.error(ResponseCode.NO_BODY), HttpStatus.BAD_REQUEST);
    }

    // Default: Internal error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleGenericException(Exception ex, WebRequest request) {
        ex.printStackTrace(); // log để debug
        return buildErrorResponse(ResponseCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Response<Void>> buildErrorResponse(ResponseCode code) {
        return new ResponseEntity<>(Response.error(code), code.getStatus());
    }
}
