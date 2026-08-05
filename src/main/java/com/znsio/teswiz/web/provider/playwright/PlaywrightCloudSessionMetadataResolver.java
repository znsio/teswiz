package com.znsio.teswiz.web.provider.playwright;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;

public final class PlaywrightCloudSessionMetadataResolver {
    private static final String BROWSERSTACK_SESSION_DETAILS =
            "browserstack_executor: {\"action\": \"getSessionDetails\"}";
    private static final String LAMBDATEST_TEST_DETAILS =
            "lambdatest_action: {\"action\": \"getTestDetails\"}";

    public Map<String, String> resolve(JavascriptExecutor executor, String providerName) {
        if (null == providerName || providerName.isBlank() || "local".equalsIgnoreCase(providerName)) {
            return Map.of();
        }

        return switch (providerName.toLowerCase()) {
            case "browserstack" -> resolveBrowserStackMetadata(executor);
            case "lambdatest" -> resolveLambdaTestMetadata(executor);
            default -> Map.of();
        };
    }

    private Map<String, String> resolveBrowserStackMetadata(JavascriptExecutor executor) {
        JSONObject details = toJsonObject(executor.executeScript(BROWSERSTACK_SESSION_DETAILS));
        Map<String, String> metadata = new LinkedHashMap<>();
        putIfPresent(metadata, "providerSessionId", details.optString("hashed_id"));
        putIfPresent(metadata, "providerBuildId", details.optString("build_hashed_id"));
        putIfPresent(metadata, "providerReportUrl", details.optString("browser_url"));
        putIfPresent(metadata, "providerPublicUrl", details.optString("public_url"));
        putIfPresent(metadata, "providerLogsUrl", details.optString("logs"));
        putIfPresent(metadata, "providerVideoUrl", details.optString("video_url"));
        putIfPresent(metadata, "providerConsoleLogsUrl", details.optString("browser_console_logs_url"));
        putIfPresent(metadata, "providerNetworkLogsUrl", details.optString("har_logs_url"));
        putIfPresent(metadata, "providerPlaywrightLogsUrl", details.optString("playwright_logs_url"));
        return metadata;
    }

    private Map<String, String> resolveLambdaTestMetadata(JavascriptExecutor executor) {
        JSONObject response = toJsonObject(executor.executeScript(LAMBDATEST_TEST_DETAILS));
        JSONObject details = response.optJSONObject("data");
        if (null == details) {
            return Map.of();
        }
        Map<String, String> metadata = new LinkedHashMap<>();
        String sessionId = firstNonBlank(details.optString("session_id"), details.optString("test_id"));
        putIfPresent(metadata, "providerSessionId", sessionId);
        putIfPresent(metadata, "providerBuildId", details.opt("build_id"));
        putIfPresent(metadata, "providerReportUrl",
                null == sessionId || sessionId.isBlank()
                        ? null
                        : "https://automation.lambdatest.com/logs/?sessionID=" + sessionId);
        putIfPresent(metadata, "providerVideoUrl", details.optString("video_url"));
        putIfPresent(metadata, "providerConsoleLogsUrl", details.optString("console_logs_url"));
        putIfPresent(metadata, "providerNetworkLogsUrl", details.optString("network_logs_url"));
        putIfPresent(metadata, "providerCommandLogsUrl", details.optString("command_logs_url"));
        putIfPresent(metadata, "providerScreenshotUrl", details.optString("screenshot_url"));
        return metadata;
    }

    private JSONObject toJsonObject(Object rawResponse) {
        if (rawResponse instanceof JSONObject jsonObject) {
            return jsonObject;
        }
        if (null == rawResponse) {
            return new JSONObject();
        }
        String response = String.valueOf(rawResponse).trim();
        return response.isEmpty() ? new JSONObject() : new JSONObject(response);
    }

    private void putIfPresent(Map<String, String> metadata, String key, Object value) {
        if (null == value) {
            return;
        }
        String normalizedValue = String.valueOf(value).trim();
        if (!normalizedValue.isEmpty() && !"null".equalsIgnoreCase(normalizedValue)) {
            metadata.put(key, normalizedValue);
        }
    }

    private String firstNonBlank(String primaryValue, String fallbackValue) {
        return null != primaryValue && !primaryValue.isBlank() ? primaryValue : fallbackValue;
    }
}
