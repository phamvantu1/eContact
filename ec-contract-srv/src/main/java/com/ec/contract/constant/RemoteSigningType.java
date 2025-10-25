package com.ec.contract.constant;

public enum RemoteSigningType implements IDbValue<Integer> {
    YEU_CAU_KY(0), THUC_HIEN_KY(1);

    final Integer dbVal;

    RemoteSigningType(final Integer dbVal) {
        this.dbVal = dbVal;
    }

    @Override
    public Integer getDbVal() {
        return dbVal;
    }
}
