package com.ec.contract.constant;

public enum DocumentType {

    GOC(1), // file gốc
    VIEW(2),
    DINH_KEM(3);

    final Integer dbVal;

    DocumentType(Integer dbVal) {
        this.dbVal = dbVal;
    }

    public Integer getDbVal() {
        return dbVal;
    }
}
