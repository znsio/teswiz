package com.znsio.teswiz.tools;

public class StringUtils {

    public static String normaliseScenarioName(String scenarioName) {
        return scenarioName.replaceAll("[`~ !@#$%^&*()\\-=+\\[\\]{}\\\\|;:'\",<.>/?]", "_")
                .replaceAll("__", "_").replaceAll("__", "_");
    }
}
