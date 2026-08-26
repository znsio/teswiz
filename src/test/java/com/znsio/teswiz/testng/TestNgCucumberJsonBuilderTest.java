package com.znsio.teswiz.testng;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestNgCucumberJsonBuilderTest {

    @Test
    void shouldGroupScenariosByFeatureNameAndConvertStepsAndTags() {
        TestNgScenarioReportData passingScenario = new TestNgScenarioReportData(
                "TheAppInvalidLoginTestNgTest", "invalidLogin", List.of("theapp", "smoke"),
                TestNgCapturedStep.PASSED, 150L,
                List.of(new TestNgCapturedStep("AuthBL.signIn", "com.znsio.teswiz.businessLayer.AuthBL.signIn(String)",
                                0, TestNgCapturedStep.PASSED, 100_000_000L),
                        new TestNgCapturedStep("LoginScreenWeb.enterLoginDetails", "com.znsio.teswiz.screen.web.theapp.LoginScreenWeb.enterLoginDetails()",
                                1, TestNgCapturedStep.PASSED, 50_000_000L)));
        TestNgScenarioReportData failingScenario = new TestNgScenarioReportData(
                "TheAppInvalidLoginTestNgTest", "invalidLoginRetry", List.of("theapp"),
                TestNgCapturedStep.FAILED, 80L,
                List.of(new TestNgCapturedStep("AuthBL.signIn", "com.znsio.teswiz.businessLayer.AuthBL.signIn(String)",
                        0, TestNgCapturedStep.FAILED, 80_000_000L)));
        TestNgScenarioReportData otherFeatureScenario = new TestNgScenarioReportData(
                "GoogleSearchWebTestNgTest", "searchForTeswiz", List.of("web"),
                TestNgCapturedStep.PASSED, 200L,
                List.of(new TestNgCapturedStep("SearchBL.search", "com.znsio.teswiz.businessLayer.SearchBL.search(String)",
                        0, TestNgCapturedStep.PASSED, 200_000_000L)));

        JSONArray features = TestNgCucumberJsonBuilder.build(
                List.of(passingScenario, failingScenario, otherFeatureScenario));

        assertThat(features.length()).isEqualTo(2);

        JSONObject theAppFeature = findFeatureByName(features, "TheAppInvalidLoginTestNgTest");
        assertThat(theAppFeature.getString("keyword")).isEqualTo("Feature");
        assertThat(theAppFeature.getJSONArray("elements").length()).isEqualTo(2);

        JSONObject passingElement = theAppFeature.getJSONArray("elements").getJSONObject(0);
        assertThat(passingElement.getString("keyword")).isEqualTo("Scenario");
        assertThat(passingElement.getString("name")).isEqualTo("invalidLogin");
        assertThat(passingElement.getString("type")).isEqualTo("scenario");

        JSONArray tags = passingElement.getJSONArray("tags");
        assertThat(tags.length()).isEqualTo(2);
        assertThat(tags.getJSONObject(0).getString("name")).isEqualTo("@theapp");
        assertThat(tags.getJSONObject(1).getString("name")).isEqualTo("@smoke");

        JSONArray steps = passingElement.getJSONArray("steps");
        assertThat(steps.length()).isEqualTo(2);
        JSONObject firstStep = steps.getJSONObject(0);
        assertThat(firstStep.getString("name")).isEqualTo("AuthBL.signIn");
        assertThat(firstStep.getString("keyword")).isEqualTo("* ");
        assertThat(firstStep.getJSONObject("result").getString("status")).isEqualTo("passed");
        assertThat(firstStep.getJSONObject("result").getLong("duration")).isEqualTo(100_000_000L);
        assertThat(firstStep.getJSONObject("match").getString("location"))
                .isEqualTo("com.znsio.teswiz.businessLayer.AuthBL.signIn(String)");

        JSONObject nestedStep = steps.getJSONObject(1);
        String nestedStepName = nestedStep.getString("name");
        assertThat(nestedStepName)
                .as("a depth-1 step should be indented so nesting (BL -> screen) is visible in the report")
                .isNotEqualTo("LoginScreenWeb.enterLoginDetails")
                .endsWith("LoginScreenWeb.enterLoginDetails");

        JSONObject failingElement = theAppFeature.getJSONArray("elements").getJSONObject(1);
        JSONObject failingStep = failingElement.getJSONArray("steps").getJSONObject(0);
        assertThat(failingStep.getJSONObject("result").getString("status")).isEqualTo("failed");

        JSONObject googleFeature = findFeatureByName(features, "GoogleSearchWebTestNgTest");
        assertThat(googleFeature.getJSONArray("elements").length()).isEqualTo(1);
    }

    private JSONObject findFeatureByName(JSONArray features, String featureName) {
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            if (feature.getString("name").equals(featureName)) {
                return feature;
            }
        }
        throw new AssertionError("No feature found with name: " + featureName);
    }
}
