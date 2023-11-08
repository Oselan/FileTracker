package com.oselan.filetracker;

import java.security.SecureRandom;

public class Utils {
	 public static String generateRandomAlphaNumericString(int length) {
	        // You can customize the characters that you want to add into
	        // the random strings
	        // No O
	        String CHAR_LOWER = "abcdefghijklmnpqrstuvwxyz";
	        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
	        // No 0
	        String NUMBER = "123456789";

	        // String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
	        String DATA_FOR_RANDOM_STRING = CHAR_UPPER + NUMBER;

	        SecureRandom random = new SecureRandom();

	        if (length < 1)
	            throw new IllegalArgumentException();

	        StringBuilder sb = new StringBuilder(length);

	        for (int i = 0; i < length; i++) {
	            // 0-62 (exclusive), random returns 0-61
	            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
	            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

	            sb.append(rndChar);
	        }

	        return sb.toString();
	    }

}
