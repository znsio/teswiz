package com.znsio.teswiz.testng;

import java.util.List;

public record TestNgScenarioReportData(String featureName, String scenarioName, List<String> tags,
                                        String status, long durationMillis, List<TestNgCapturedStep> steps) {
}
