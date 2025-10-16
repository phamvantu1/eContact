package com.ec.library.exception;

public class CustomerApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public CustomerApiException(int statusCode, String responseBody) {
        super("Customer API lỗi: status=" + statusCode + ", body=" + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}