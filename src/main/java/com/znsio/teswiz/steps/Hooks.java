package com.znsio.teswiz.steps;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.FileLocations;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.UserPersonaDetails;
import com.znsio.teswiz.tools.ReportPortalLogger;
import com.znsio.teswiz.tools.ScenarioUtils;
import com.znsio.teswiz.tools.ScreenShotManager;
import com.znsio.teswiz.tools.cmd.AsyncCommandLineExecutor;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

import java.io.File;
import java.util.*;

public class Hooks {
    private static final Logger LOGGER = LogManager.getLogger(Hooks.class.getName());
    private static final List<String> excludeLoggingSystemProperties = Arrays.asList("java.class.path", "java.library.path");
    private static final List<String> excludeLoggingEnvVariables = Arrays.asList("KEY", "PASSWORD");
    private final Map<String, Integer> scenarioRunCounts = new HashMap<>();
    private final TestExecutionContext testExecutionContext;
    private final long threadId;

    public Hooks() {
        threadId = Thread.currentThread().getId();
        testExecutionContext = Runner.getTestExecutionContext(threadId);
    }

    public void beforeScenario(Scenario scenario) {
        String scenarioName = scenario.getName();
        Integer scenarioRunCount = getScenarioRunCount(scenarioName);
        TestExecutionContext testExecutionContext = new TestExecutionContext(scenarioRunCount + "-" + scenarioName);
        String normalisedScenarioName = ScenarioUtils.normaliseScenarioName(scenarioName);

        LOGGER.info(String.format("ThreadId: %d: beforeScenario: for scenario: %s%n", Thread.currentThread().getId(), scenarioName));
        String scenarioLogDirectory = FileLocations.REPORTS_DIRECTORY + normalisedScenarioName;
        testExecutionContext.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioLogDirectory);
        String screenshotDirectory = FileLocations.REPORTS_DIRECTORY + normalisedScenarioName + File.separator + "screenshot" + File.separator;
        testExecutionContext.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY, screenshotDirectory);

        Object isHooksInitialized = testExecutionContext.getTestState(TEST_CONTEXT.HOOKS_INITIALIZED);
        LOGGER.info("Hooks: beforeScenario: isHooksInitialized: " + isHooksInitialized);
        if (null == isHooksInitialized) {
            LOGGER.info("Hooks: ThreadId : '%d' :: beforeScenario: '%s'".formatted(threadId, scenarioName));
            if (!Runner.isAPI() || !Runner.isCLI() || !Runner.isPDF()) {
                testExecutionContext.addTestState(TEST_CONTEXT.SCREENSHOT_MANAGER, new ScreenShotManager());
            }
            testExecutionContext.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS,
                                              new UserPersonaDetails());
            SoftAssertions softly = new SoftAssertions();
            testExecutionContext.addTestState(TEST_CONTEXT.SOFT_ASSERTIONS, softly);
            addEnvironmentVariablesToReportPortal();
            addSystemPropertiesToReportPortal();
            startTheAsyncCommandLineExecutor();
            testExecutionContext.addTestState(TEST_CONTEXT.HOOKS_INITIALIZED, true);
        }
    }

    public void afterScenario(Scenario scenario) {
        Object isHooksInitialized = testExecutionContext.getTestState(TEST_CONTEXT.HOOKS_INITIALIZED);
        LOGGER.info("Hooks: afterScenario: isHooksInitialized: " + isHooksInitialized);
        if (null != isHooksInitialized) {
            LOGGER.info("Hooks: ThreadId : '%d' :: afterScenario: '%s'".formatted(threadId, scenario.getName()));
            testExecutionContext.addTestState(TEST_CONTEXT.HOOKS_INITIALIZED, null);
            Drivers.attachLogsAndCloseAllDrivers(scenario);
            closeTheAsyncCommandLineExecutor();
            SoftAssertions softly = Runner.getSoftAssertion(threadId);
            LOGGER.info("Hooks: Assert all soft assertions");
            softly.assertAll();
        }
        SessionContext.remove(Thread.currentThread().getId());
    }

    private Integer getScenarioRunCount(String scenarioName) {
        if (scenarioRunCounts.containsKey(scenarioName)) {
            scenarioRunCounts.put(scenarioName, scenarioRunCounts.get(scenarioName) + 1);
        } else {
            scenarioRunCounts.put(scenarioName, 1);
        }
        return scenarioRunCounts.get(scenarioName);
    }

    private void startTheAsyncCommandLineExecutor() {
        if (Runner.isCLI()) {
            LOGGER.info("Start the AsyncCommandLineExecutor");
            AsyncCommandLineExecutor asyncCommandLineExecutor = new AsyncCommandLineExecutor();
            testExecutionContext.addTestState(TEST_CONTEXT.ASYNC_COMMAND_LINE_EXECUTOR, asyncCommandLineExecutor);
        }
    }

    private void addEnvironmentVariablesToReportPortal() {
        Map<String, String> envVars = System.getenv();
        StringBuilder envVarInfo = new StringBuilder();

        envVars.entrySet().stream()
                .filter(entry -> excludeLoggingEnvVariables.stream().noneMatch(excludedKey ->
                        entry.getKey().toLowerCase().contains(excludedKey.toLowerCase())))
                .forEach(entry -> envVarInfo.append("\t").append(entry.getKey()).append(":").append(entry.getValue()).append("\n"));

        envVars.entrySet().stream()
                .filter(entry -> excludeLoggingEnvVariables.stream().anyMatch(excludedKey ->
                        entry.getKey().toLowerCase().contains(excludedKey.toLowerCase())))
                .forEach(entry -> envVarInfo.append("\t").append(entry.getKey()).append(":").append("*****").append("\n"));

        ReportPortalLogger.logDebugMessage(
                String.format("Hooks: Environment Variables:%n%s", envVarInfo));
    }

    private void addSystemPropertiesToReportPortal() {
        Properties props = System.getProperties();
        StringBuilder propVars = new StringBuilder();

        props.entrySet().stream()
                .filter(entry -> excludeLoggingSystemProperties.stream().noneMatch(excludedKey ->
                        entry.getKey().toString().toLowerCase().contains(excludedKey.toLowerCase())))
                .forEach(entry -> propVars.append("\t").append(entry.getKey()).append(":").append(entry.getValue()).append("\n"));

        props.entrySet().stream()
                .filter(entry -> excludeLoggingSystemProperties.stream().anyMatch(excludedKey ->
                        entry.getKey().toString().toLowerCase().contains(excludedKey.toLowerCase())))
                .forEach(entry -> propVars.append("\t").append(entry.getKey()).append(":").append("*****").append("\n"));

        ReportPortalLogger.logDebugMessage(
                String.format("Hooks: System Properties:%n%s", propVars));
    }

    private void closeTheAsyncCommandLineExecutor() {
        if (Runner.isCLI()) {
            LOGGER.info("Close the AsyncCommandLineExecutor");
            AsyncCommandLineExecutor asyncCommandLineExecutor = (AsyncCommandLineExecutor) testExecutionContext.getTestState(TEST_CONTEXT.ASYNC_COMMAND_LINE_EXECUTOR);
            asyncCommandLineExecutor.close();
        }
    }

}
