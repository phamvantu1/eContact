package com.ec.library.constants;

public class CommonConstants {

    public static class SubjectEmail {
        public static final String CREATE_CONTRACT = "Tạo mới hợp đồng thành công";
        public static final String UPDATE_CONTRACT = "Cập nhật hợp đồng thành công";
        public static final String ABOUT_EXPIRY_CONTRACT = "Hợp đồng sắp hết hạn";
        public static final String EXPIRED_CONTRACT = "Hợp đồng đã hết hạn";
    }

    public static class CodeEmail {
        public static final String CREATE_CONTRACT = "CREATE_CONTRACT";
        public static final String UPDATE_CONTRACT = "UPDATE_CONTRACT";
        public static final String ABOUT_EXPIRY_CONTRACT = "ABOUT_EXPIRY_CONTRACT";
        public static final String EXPIRED_CONTRACT = "EXPIRED_CONTRACT";
        public static final String EMAIL = "EMAIL";
    }

    public static class TitleEmail {
        public static final String VIEW_CONTRACT = "Bạn có hợp đồng {status}";
        public static final String APPROVE = "approve";
        public static final String SIGN = "sign";
    }

    public static class ActionButton {
        public static final String VIEW_CONTRACT = "Xem hợp đồng";
    }

    public static class url {
        public static final String VIEW_CONTRACT = "/contracts/view/";
    }
}
