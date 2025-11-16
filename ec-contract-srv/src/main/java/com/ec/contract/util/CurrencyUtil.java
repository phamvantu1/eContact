package com.ec.contract.util;

public final class CurrencyUtil {
    public static String format(long number) {
        return String.format("%,d", number).replaceAll(",", ".");
    }
}
