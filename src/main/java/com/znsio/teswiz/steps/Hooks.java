package com.znsio.teswiz.steps;

import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.UserPersonaDetails;
import com.znsio.teswiz.tools.ReportPortalLogger;
import com.znsio.teswiz.tools.ScreenShotManager;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.assertj.core.api.SoftAssertions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Hooks {
    private static final Logger LOGGER = LogManager.getLogger(Hooks.class.getName());
    private static final List<String> excludeLoggingSystemProperties = Arrays.asList("java.class.path", "java.library.path");
    private static final List<String> excludeLoggingEnvVariables = Arrays.asList("KEY", "PASSWORD");

    public void beforeScenario(Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        TestExecutionContext testExecutionContext = Runner.getTestExecutionContext(threadId);
        LOGGER.info(String.format("Hooks: ThreadId : %s In RunCukes - beforeScenario: %s", threadId,
                                  scenario.getName()));
        LOGGER.info(String.format("Hooks: Running test %s on %s", testExecutionContext.getTestName(),
                                  Runner.getPlatform().name()));
        if(!Runner.getPlatform().equals(Platform.api)) {
            testExecutionContext.addTestState(TEST_CONTEXT.SCREENSHOT_MANAGER, new ScreenShotManager());
        }
        testExecutionContext.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS,
                                          new UserPersonaDetails());
        SoftAssertions softly = new SoftAssertions();
        testExecutionContext.addTestState(TEST_CONTEXT.SOFT_ASSERTIONS, softly);
        addEnvironmentVariablesToReportPortal();
        addSystemPropertiesToReportPortal();
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

    public void afterScenario(Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        LOGGER.info(String.format("Hooks: ThreadId: %d In RunCukes - afterScenario: %s", threadId,
                                  scenario.getName()));
        Drivers.attachLogsAndCloseAllDrivers(scenario);
        SoftAssertions softly = Runner.getSoftAssertion(threadId);
        LOGGER.info("Hooks: Assert all soft assertions");
        softly.assertAll();
    }
}
