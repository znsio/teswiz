package com.znsio.teswiz.tools;

public class ScenarioUtils {
    public static String normaliseScenarioName(String scenarioName) {
        return scenarioName.replaceAll("[`~ !@#$%^&*()\\-=+\\[\\]{}\\\\|;:'\",<.>/?]", "_")
                .replaceAll("__", "_").replaceAll("__", "_");
    }
}
