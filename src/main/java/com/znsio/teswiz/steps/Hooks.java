package com.znsio.teswiz.steps;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.UserPersonaDetails;
import com.znsio.teswiz.tools.JsonPrettyPrinter;
import com.znsio.teswiz.tools.ReportPortalLogger;
import com.znsio.teswiz.tools.ScreenShotManager;
import com.znsio.teswiz.tools.SensitiveDataMasker;
import com.znsio.teswiz.tools.cmd.AsyncCommandLineExecutor;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Hooks {
    private static final Logger LOGGER = LogManager.getLogger(Hooks.class.getName());
    private static final List<String> excludeLoggingSystemProperties = Arrays.asList("java.class.path", "java.library.path");
    private static final List<String> excludeLoggingEnvVariables = Arrays.asList("KEY", "PASSWORD");
    private final TestExecutionContext testExecutionContext;
    private final long threadId;

    public Hooks() {
        threadId = Thread.currentThread().getId();
        testExecutionContext = Runner.getTestExecutionContext(threadId);
    }

    public void beforeScenario(Scenario scenario) {
        Object isHooksInitialized = testExecutionContext.getTestState(TEST_CONTEXT.HOOKS_INITIALIZED);
        LOGGER.info("Hooks: beforeScenario: isHooksInitialized: " + isHooksInitialized);
        if (null == isHooksInitialized) {
            LOGGER.info("Hooks: ThreadId : '%d' :: beforeScenario: '%s'".formatted(threadId, scenario.getName()));
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
    }

    private void startTheAsyncCommandLineExecutor() {
        if (Runner.isCLI()) {
            LOGGER.info("Start the AsyncCommandLineExecutor");
            testExecutionContext.addTestState(TEST_CONTEXT.CLI_COMMAND_NUMBER, 1);
            AsyncCommandLineExecutor asyncCommandLineExecutor = new AsyncCommandLineExecutor();
            testExecutionContext.addTestState(TEST_CONTEXT.ASYNC_COMMAND_LINE_EXECUTOR, asyncCommandLineExecutor);
        }
    }

    private void addEnvironmentVariablesToReportPortal() {
        Map<String, String> envVars = System.getenv();
        Map<String, String> maskedEnvVars = new LinkedHashMap<>();
        envVars.forEach((key, value) -> {
            if (isSensitiveProperty(key)) {
                maskedEnvVars.put(key, "*****");
            } else {
                maskedEnvVars.put(key, value);
            }
        });

        ReportPortalLogger.logDebugMessage(
                String.format("Hooks: Environment Variables:%n%s",
                        SensitiveDataMasker.mask(JsonPrettyPrinter.prettyPrint(maskedEnvVars))));
    }

    private void addSystemPropertiesToReportPortal() {
        Properties props = System.getProperties();
        Map<String, String> maskedProperties = new LinkedHashMap<>();
        props.forEach((key, value) -> {
            String propertyName = String.valueOf(key);
            if (isSensitiveProperty(propertyName)) {
                maskedProperties.put(propertyName, "*****");
            } else {
                maskedProperties.put(propertyName, String.valueOf(value));
            }
        });

        ReportPortalLogger.logDebugMessage(
                String.format("Hooks: System Properties:%n%s",
                        SensitiveDataMasker.mask(JsonPrettyPrinter.prettyPrint(maskedProperties))));
    }

    private boolean isSensitiveProperty(String propertyName) {
        String normalizedPropertyName = propertyName.toLowerCase();
        return excludeLoggingSystemProperties.stream().anyMatch(excludedKey ->
                normalizedPropertyName.contains(excludedKey.toLowerCase()))
                || excludeLoggingEnvVariables.stream().anyMatch(excludedKey ->
                normalizedPropertyName.contains(excludedKey.toLowerCase()))
                || normalizedPropertyName.contains("token")
                || normalizedPropertyName.contains("secret")
                || normalizedPropertyName.contains("auth")
                || normalizedPropertyName.contains("credential")
                || normalizedPropertyName.contains("apikey");
    }

    private void closeTheAsyncCommandLineExecutor() {
        if (Runner.isCLI()) {
            LOGGER.info("Close the AsyncCommandLineExecutor");
            AsyncCommandLineExecutor asyncCommandLineExecutor = (AsyncCommandLineExecutor) testExecutionContext.getTestState(TEST_CONTEXT.ASYNC_COMMAND_LINE_EXECUTOR);
            asyncCommandLineExecutor.close();
        }
    }

}
