package com.ec.library.constants;

public class CommonConstants {

    public static class SubjectEmail {
        public static final String CREATE_CONTRACT = "Tạo mới hợp đồng thành công";
        public static final String UPDATE_CONTRACT = "Cập nhật hợp đồng thành công";
        public static final String ABOUT_EXPIRY_CONTRACT = "Hợp đồng sắp hết hạn";
        public static final String EXPIRED_CONTRACT = "Hợp đồng đã hết hạn";
        public static final String COORDINATOR = "Hợp đồng điều phối";
        public static final String REVIEWER = "Hợp đồng yêu cầu xem xét";
        public static final String SIGNER = "Hợp đồng chờ ký";
        public static final String ARCHIVER = "Hợp đồng cần đóng dấu";
        public static final String REJECTED_CONTRACT = "Hợp đồng bị từ chối";
        public static final String AUTHORIZE_CONTRACT = "Hợp đồng được ủy quyền";
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
        public static final String COORDINATOR = "Hợp đồng yêu cầu điều phối";
        public static final String REVIEWER = "Hợp đồng yêu cầu xem xét";
        public static final String SIGNER = "Hợp đồng yêu cầu ký";
        public static final String ARCHIVER = "Hợp đồng yêu cầu đóng dấu";
        public static final String REJECTED_CONTRACT = "Hợp đồng bị từ chối";
        public static final String AUTHORIZE_CONTRACT = "Hợp đồng được ủy quyền";
    }

    public static class ActionButton {
        public static final String VIEW_CONTRACT = "Xem hợp đồng";
    }

    public static class url {
        public static final String VIEW_CONTRACT = "/contracts/view/";
        public static final String SIGNER = "/contracts/signer/";
        public static final String REVIEWER = "/contracts/approve/";
        public static final String COORDINATOR = "/contracts/coordinator/";
        public static final String ARCHIVER = "/contracts/archiver/";
        public static final String REJECTED_CONTRACT = "/contracts/rejected/";
        public static final String AUTHORIZE_CONTRACT = "/contracts/authorize/";
    }
}
