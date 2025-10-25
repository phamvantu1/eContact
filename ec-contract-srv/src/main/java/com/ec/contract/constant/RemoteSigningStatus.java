package com.ec.contract.constant;

public enum RemoteSigningStatus implements IDbValue<Integer> {
    DANG_XU_LY(0), HOAN_THANH(1), THAT_BAI(2), QUA_THOI_GIAN_KY(3),HUY_BO(4),TU_CHOI(5);

    final Integer dbVal;

    RemoteSigningStatus(Integer dbVal) {
        this.dbVal = dbVal;
    }

    public Integer getDbVal() {
        return dbVal;
    }

    public String getViLabel() {
        switch (this) {
            case DANG_XU_LY:
                return "Đang xử lý";
            case HOAN_THANH:
                return "Hoàn thành";
            case THAT_BAI:
                return "Thất bại";
            case QUA_THOI_GIAN_KY:
                return "Quá thời gian ký";
            case HUY_BO:
                return "Hủy Bỏ";
            case TU_CHOI:
                return "Từ chối hợp đồng phía CA2 RS";
        }
        return "";
    }

}

