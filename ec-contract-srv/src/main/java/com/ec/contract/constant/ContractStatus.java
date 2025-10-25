package com.ec.contract.constant;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public enum ContractStatus implements IDbValue<Integer> {
    DRAFT(0),
    CREATED(10),
    PROCESSING(20),
    SIGNED(30),
    LIQUIDATED(40),
    REJECTED(31),
    CANCEL(32),
    ABOUT_EXPRIRE(1),
    EXPRIRE(2),
    SCAN(35);
    final Integer dbVal;

    ContractStatus(Integer dbVal) {
        this.dbVal = dbVal;
    }

    public Integer getDbVal() {
        return dbVal;
    }

    public String getViLabel() {
        switch (this) {
            case DRAFT:
                return "Bản nháp";
            case CREATED:
                return "Tạo";
            case PROCESSING:
                return "Đang xử lý";
            case SIGNED:
                return "Hoàn thành";
            case LIQUIDATED:
                return "Thanh lý";
            case REJECTED:
                return "Từ chối";
            case CANCEL:
                return "Hủy bỏ";
            case ABOUT_EXPRIRE:
                return "sắp hết hạn";
            case EXPRIRE:
                return "Quá hạn";
            case SCAN:
                return "Lưu trữ";
        }
        return "";
    }
}
