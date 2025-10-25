package com.ec.contract.constant;

public enum SignType implements IDbValue<Integer> {
    IMAGE_AND_OTP(1), USB_TOKEN(2), SIM_PKI(3), HSM(4), EKYC(5), CERT(6), BAN_CO_YEU(7), REMOTE_SIGNING(8);

    final Integer dbVal;

    SignType(final Integer dbVal) {
        this.dbVal = dbVal;
    }

    @Override
    public Integer getDbVal() {
        return dbVal;
    }

    public static String getInfo(int idType) {
        switch (idType) {
            case 1:
                return "Ký ảnh và OTP";
            case 2:
                return "Ký số bằng USB token";
            case 3:
                return "Ký số bằng sim PKI";
            case 4:
                return "Ký số bằng HSM";
            case 5:
                return "Ký eKYC";
            case 6:
                return "Ký bằng chứng thư số server";
            case 7:
                return "Ký số bằng USB token Ban Cơ Yếu";
            case 8:
                return "Ký số Remote Signing";
        }
        return "";
    }
}