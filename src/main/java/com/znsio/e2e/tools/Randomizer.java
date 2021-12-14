package com.znsio.e2e.tools;

import org.apache.commons.lang3.RandomStringUtils;

public class Randomizer {

    public static String randomize (int size) {
        return RandomStringUtils.randomNumeric(size);
    }

    public static String randomize (String randomizeTestData) {
        String randomizedValue = randomizeTestData;
        try {
            Long.parseLong(randomizeTestData);
            randomizedValue = "80" + RandomStringUtils.randomNumeric(8);
        } catch (NumberFormatException nfe) {
            randomizedValue = "e2e_" + RandomStringUtils.randomAlphanumeric(10) + "@getnada.com";
        }
        return randomizedValue;
    }
}
