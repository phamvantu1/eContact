package com.ec.contract.constant;

public enum DocumentType implements IDbValue<Integer> {

    PRIMARY(1),
    FINALLY(2),
    ATTACH(3),
    BATCH(4),
    COMPRESS(5),
    BACKUP(6),
    IMG_EKYC(7),
    HISTORY(8);

    final Integer dbVal;

    DocumentType(final Integer dbVal) {
        this.dbVal = dbVal;
    }

    @Override
    public Integer getDbVal() {
        return dbVal;
    }
}
