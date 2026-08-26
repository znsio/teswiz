package com.znsio.teswiz.testng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Builds a synthetic Cucumber-JSON-message document from TestNG scenario data so
// masterthought's cucumber-reporting ReportBuilder (which only understands that
// schema) can render TestNG-mode results as a rich HTML report. Each business-layer
// call captured by TestNgStepCaptureAspect becomes one Cucumber-style step.
public final class TestNgCucumberJsonBuilder {
    // Cucumber's JSON step list is flat, so nesting (test -> BL -> screen -> ...)
    // is conveyed visually by indenting the step name text itself. A real
    // non-breaking space (U+00A0, not a regular space) is used per indent level so
    // HTML rendering doesn't collapse it away.
    private static final String INDENT_UNIT = "    ";
    private static final String NESTED_MARKER = "↳ ";

    private TestNgCucumberJsonBuilder() { }

    public static JSONArray build(List<TestNgScenarioReportData> scenarios) {
        Map<String, List<TestNgScenarioReportData>> scenariosByFeature = new LinkedHashMap<>();
        for (TestNgScenarioReportData scenario : scenarios) {
            scenariosByFeature.computeIfAbsent(scenario.featureName(), name -> new java.util.ArrayList<>()).add(scenario);
        }

        JSONArray features = new JSONArray();
        scenariosByFeature.forEach((featureName, featureScenarios) -> features.put(toFeature(featureName, featureScenarios)));
        return features;
    }

    private static JSONObject toFeature(String featureName, List<TestNgScenarioReportData> scenarios) {
        JSONObject feature = new JSONObject();
        feature.put("line", 1);
        feature.put("uri", featureName);
        feature.put("id", featureName.toLowerCase());
        feature.put("keyword", "Feature");
        feature.put("name", featureName);
        feature.put("description", "");

        JSONArray elements = new JSONArray();
        scenarios.forEach(scenario -> elements.put(toElement(scenario)));
        feature.put("elements", elements);
        return feature;
    }

    private static JSONObject toElement(TestNgScenarioReportData scenario) {
        JSONObject element = new JSONObject();
        element.put("line", 1);
        element.put("id", scenario.scenarioName().toLowerCase());
        element.put("type", "scenario");
        element.put("keyword", "Scenario");
        element.put("name", scenario.scenarioName());
        element.put("description", "");
        element.put("steps", toSteps(scenario.steps()));
        element.put("tags", toTags(scenario.tags()));
        return element;
    }

    private static JSONArray toTags(List<String> tags) {
        JSONArray jsonTags = new JSONArray();
        tags.forEach(tag -> jsonTags.put(new JSONObject().put("name", "@" + tag)));
        return jsonTags;
    }

    private static JSONArray toSteps(List<TestNgCapturedStep> steps) {
        JSONArray jsonSteps = new JSONArray();
        int line = 1;
        for (TestNgCapturedStep step : steps) {
            JSONObject jsonStep = new JSONObject();
            jsonStep.put("keyword", "* ");
            jsonStep.put("line", line++);
            jsonStep.put("name", indentedStepName(step));
            JSONObject match = new JSONObject();
            match.put("location", step.matchLocation());
            jsonStep.put("match", match);
            JSONObject result = new JSONObject();
            result.put("status", step.status());
            result.put("duration", step.durationNanos());
            jsonStep.put("result", result);
            jsonSteps.put(jsonStep);
        }
        return jsonSteps;
    }

    private static String indentedStepName(TestNgCapturedStep step) {
        if (step.depth() == 0) {
            return step.stepName();
        }
        return INDENT_UNIT.repeat(step.depth()) + NESTED_MARKER + step.stepName();
    }
}
