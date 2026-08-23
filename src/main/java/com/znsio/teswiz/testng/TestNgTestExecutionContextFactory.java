package com.znsio.teswiz.testng;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.FileLocations;
import com.znsio.teswiz.tools.FileUtils;
import com.znsio.teswiz.tools.OsUtils;
import com.znsio.teswiz.tools.StringUtils;

import java.io.File;

public final class TestNgTestExecutionContextFactory {
    private TestNgTestExecutionContextFactory() { }

    public static TestExecutionContext create(String testName, int runningTestNumber) {
        TestExecutionContext context = new TestExecutionContext(testName + "-" + runningTestNumber);
        String normalisedTestName = StringUtils.normaliseScenarioName(testName);

        String testLogDirectory = FileLocations.REPORTS_DIRECTORY + runningTestNumber + "-" + normalisedTestName + File.separator;
        String screenshotDirectory = testLogDirectory + FileLocations.SCREENSHOTS_DIRECTORY;
        String deviceLogsDirectory = testLogDirectory + FileLocations.DEVICE_LOGS_DIRECTORY;

        testLogDirectory = FileUtils.createDirectoryIn(OsUtils.getUserDirectory(), testLogDirectory).getAbsolutePath();
        screenshotDirectory = FileUtils.createDirectoryIn(OsUtils.getUserDirectory(), screenshotDirectory).getAbsolutePath();
        deviceLogsDirectory = FileUtils.createDirectoryIn(OsUtils.getUserDirectory(), deviceLogsDirectory).getAbsolutePath();

        context.addTestState(TEST_CONTEXT.SCENARIO_RUN_COUNT, runningTestNumber);
        context.addTestState(TEST_CONTEXT.NORMALISED_SCENARIO_NAME, normalisedTestName);
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, testLogDirectory);
        context.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY, screenshotDirectory);
        context.addTestState(TEST_CONTEXT.DEVICE_LOGS_DIRECTORY, deviceLogsDirectory);
        return context;
    }
}
