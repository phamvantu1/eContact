package com.ec.customer.common.constant;

public enum CustomerStatus {
    ACTIVE(1),
    INACTIVE(0);

    final Integer dbVal;

    CustomerStatus(Integer dbVal) {
        this.dbVal = dbVal;
    }

    public Integer getDbVal() {
        return dbVal;
    }
}

