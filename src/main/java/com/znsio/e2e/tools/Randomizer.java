package com.znsio.e2e.tools;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public class Randomizer {

    public static String randomize(int size) {
        return RandomStringUtils.randomNumeric(size);
    }

    public static String randomize(String randomizeTestData) {
        String randomizedValue;
        try {
            Long.parseLong(randomizeTestData);
            randomizedValue = "80" + RandomStringUtils.randomNumeric(8);
        } catch(NumberFormatException nfe) {
            randomizedValue = "e2e_" + RandomStringUtils.randomAlphanumeric(10) + "@getnada.com";
        }
        return randomizedValue;
    }

    public static String randomizeAlphaNumericString(int stringLength) {
        return RandomStringUtils.randomAlphanumeric(stringLength);
    }

    public static String randomizeString(int stringLength) {
        return RandomStringUtils.randomAlphabetic(stringLength);
    }

    public static int getRandomNumberBetween(int min, int max) {
        return RandomUtils.nextInt(min, max);
    }

    public static long getRandomNumberBetween(long min, long max) {
        return RandomUtils.nextLong(min, max);
    }
}
