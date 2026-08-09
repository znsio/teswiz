package com.znsio.teswiz.visual;

import java.util.List;
import java.util.Map;

import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.RectangleSize;

public record PlaywrightVisualSessionRequest(
        String appName,
        String testName,
        String serverUrl,
        String apiKey,
        String branchName,
        String environmentName,
        String baselineEnvName,
        MatchLevel defaultMatchLevel,
        boolean saveNewTests,
        boolean enabled,
        boolean verboseLogs,
        boolean useUfg,
        int testConcurrency,
        String proxyUrl,
        String logFilePath,
        RectangleSize viewportSize,
        BatchMetadata batchMetadata,
        Map<String, String> customProperties,
        List<UfgTarget> ufgTargets) {

    public record BatchMetadata(String name, String id, Map<String, String> properties) {
    }

    public record UfgTarget(
            Integer width,
            Integer height,
            String browserType,
            String deviceName,
            String screenOrientation) {
    }
}
