package com.ec.customer.common.constant;

public enum DefineStatus {
    ACTIVE(1),
    INACTIVE(0);

    private final int status;

    DefineStatus(int status) {
        this.status = status;
    }
    public int getValue() {
        return status;
    }

    public static DefineStatus fromLevel(int status) {
        for (DefineStatus s : DefineStatus.values()) {
            if (s.getValue() == status) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + status);
    }
}
