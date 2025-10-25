package com.ec.contract.constant;

/**
 * Vai trò của người tham gia ký hơp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public enum RecipientRole implements IDbValue<Integer> {
    COORDINATOR(1), REVIEWER(2), SIGNER(3), ARCHIVER(4), DELEGACY(5),CREATOR(0), CANCELLER(6);;

    final Integer dbVal;

    RecipientRole(final Integer dbVal) {
        this.dbVal = dbVal;
    }

    @Override
    public Integer getDbVal() {
        return dbVal;
    }

    public String getViLabel() {
        switch (this) {
            case COORDINATOR:
                return "Người điều phối";
            case REVIEWER:
                return "Người xem xét";
            case SIGNER:
                return "Người ký";
            case ARCHIVER:
                return "Văn thư";
            case DELEGACY:
                return "Người ủy quyền";
            case CREATOR:
                return "Người tạo";
            case CANCELLER:
                return "Người hủy bỏ";

        }
        return "";
    }
}
