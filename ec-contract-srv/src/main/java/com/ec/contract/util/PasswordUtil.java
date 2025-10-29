package com.ec.contract.util;

import java.util.Random;

public class PasswordUtil {
    private static final String NUMBERS = "1234567890";

    public static String generateToken(int length) {
        String combinedChars = NUMBERS;
        Random random = new Random();
        char[] password = new char[length];

        for(int i = 0; i< length ; i++) {
            password[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
        }
        return String.valueOf(password);
    }
}
