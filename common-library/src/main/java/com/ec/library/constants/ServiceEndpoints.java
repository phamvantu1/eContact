package com.ec.library.constants;

public class ServiceEndpoints {

    // Tên service — trùng với tên đăng ký trong Eureka
    public static final String CUSTOMER_SERVICE = "http://ec-customer-srv/api/customers";
    public static final String NOTICE_SERVICE = "http://ec-notification-srv/api/notifications";
    public static final String AUTH_SERVICE = "http:/ec-auth-srv/api/auth";
    public static final String CONTRACT_SERVICE = "http://ec-contract-srv/api/contracts";

    // Base API paths
    public static final String CUSTOMER_API = CUSTOMER_SERVICE ;
    public static final String NOTICE_API = NOTICE_SERVICE + "/api/notification";
    public static final String AUTH_API = AUTH_SERVICE + "/api/auth";
    public static final String CONTRACT_API = CONTRACT_SERVICE + "/api/contracts";

    private ServiceEndpoints() {} // Ngăn khởi tạo class
}
