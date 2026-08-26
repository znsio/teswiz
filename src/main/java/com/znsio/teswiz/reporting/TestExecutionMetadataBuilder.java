package com.znsio.teswiz.reporting;

import com.znsio.teswiz.runner.DeviceSetup;
import com.znsio.teswiz.runner.Setup;

import java.util.HashMap;
import java.util.Map;

import static com.znsio.teswiz.runner.Setup.*;

// Shared test-execution metadata (environment, platform, build info, aggregated
// session info) shown on the "Features" overview page of a masterthought rich
// report. Used by both Cucumber mode (via CustomReports) and TestNG mode (via
// TestNgCucumberStyleReportWriter) so the two report writers stay consistent.
public final class TestExecutionMetadataBuilder {
    private TestExecutionMetadataBuilder() { }

    public static Map<String, Object> build(String reportsDir) {
        HashMap<String, Object> testRunMetadata = new HashMap<>();
        testRunMetadata.put(TARGET_ENVIRONMENT, Setup.getFromConfigs(TARGET_ENVIRONMENT));
        testRunMetadata.put(PLATFORM, Setup.getFromConfigs(PLATFORM));
        testRunMetadata.put(WEB_ENGINE, Setup.getFromConfigs(WEB_ENGINE));
        testRunMetadata.put(TAG, Setup.getFromConfigs(TAG_FOR_REPORTPORTAL));
        testRunMetadata.put(RUN_IN_CI, Setup.getBooleanValueAsStringFromConfigs(RUN_IN_CI));
        testRunMetadata.put("CLOUD_NAME", DeviceSetup.getCloudNameFromCapabilities());
        testRunMetadata.put(EXECUTED_ON, Setup.getFromConfigs(EXECUTED_ON));
        testRunMetadata.put(IS_VISUAL, Setup.getBooleanValueAsStringFromConfigs(IS_VISUAL));
        testRunMetadata.put(SET_HARD_GATE, Setup.getBooleanValueAsStringFromConfigs(SET_HARD_GATE));
        testRunMetadata.put(IS_FAILING_TEST_SUITE, Setup.getBooleanValueAsStringFromConfigs(IS_FAILING_TEST_SUITE));
        testRunMetadata.put(PARALLEL, Setup.getIntegerValueFromConfigs(PARALLEL));
        testRunMetadata.put("OS", System.getProperty("os.name"));
        testRunMetadata.put(HOST_NAME, Setup.getHostMachineName());
        testRunMetadata.put(BUILD_ID, Setup.getFromConfigs(BUILD_ID));
        testRunMetadata.put(BUILD_INITIATION_REASON, Setup.getFromConfigs(BUILD_INITIATION_REASON));
        testRunMetadata.putAll(ScenarioSessionMetadataAggregator.aggregate(reportsDir));
        return testRunMetadata;
    }
}
