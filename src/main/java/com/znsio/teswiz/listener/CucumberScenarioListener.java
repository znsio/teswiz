package com.znsio.teswiz.listener;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.AppiumServerManager;
import com.znsio.teswiz.runner.FileLocations;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.tools.FileUtils;
import com.znsio.teswiz.tools.OsUtils;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CucumberScenarioListener implements ConcurrentEventListener {
    private static final Logger LOGGER = Logger.getLogger(CucumberScenarioListener.class.getName());
    private final Map<String, Integer> numberOfExamplesForScenario = new HashMap<String, Integer>();
    private int runningScenarioNumber = 0;

    public CucumberScenarioListener() {
        LOGGER.info(String.format("ThreadID: %d: CucumberScenarioListener%n", Thread.currentThread().getId()));
        setLog4jCompatibility();
        FileUtils.createDirectoryIn(OsUtils.getUserDirectory(), FileLocations.OUTPUT_DIRECTORY);
    }

    private void setLog4jCompatibility() {
        // Migrating from Log4j 1.x to 2.x - https://logging.apache.org/log4j/2.x/manual/migration.html
        System.setProperty("log4j1.compatibility", "true");
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunStarted.class, this::runStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::scenarioStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::scenarioFinishedHandler);
        eventPublisher.registerHandlerFor(TestRunFinished.class, this::runFinishedHandler);
    }

    private void runStartedHandler(TestRunStarted event) {
        LOGGER.info("runStartedHandler");
        LOGGER.info(String.format("ThreadID: %d: beforeSuite: %n", Thread.currentThread().getId()));
    }

    private void scenarioStartedHandler(TestCaseStarted event) {
        String scenarioName = event.getTestCase().getName();
        Integer currentExampleRowNumberForScenario = updateCurrentExampleRowNumberForScenario(scenarioName);
        runningScenarioNumber++;

        LOGGER.info("Running Scenario #" + runningScenarioNumber + " '" + scenarioName + "' started");
        LOGGER.info("\tCurrent Example Row Number: " + currentExampleRowNumberForScenario);
        TestExecutionContext testExecutionContext = new TestExecutionContext(scenarioName + "-" + currentExampleRowNumberForScenario);

        String normalisedScenarioName = normaliseScenarioName(scenarioName);
        String scenarioLogDirectory = FileLocations.REPORTS_DIRECTORY + runningScenarioNumber + "-" + normalisedScenarioName + "_" + currentExampleRowNumberForScenario + File.separator;
        String screenshotDirectory = scenarioLogDirectory + FileLocations.SCREENSHOTS_DIRECTORY;
        String deviceLogsDirectory = scenarioLogDirectory + FileLocations.DEVICE_LOGS_DIRECTORY;

        scenarioLogDirectory = FileUtils.createDirectoryIn(OsUtils.getUserDirectory(), scenarioLogDirectory).getAbsolutePath();
        screenshotDirectory = FileUtils.createDirectoryIn(OsUtils.getUserDirectory(), screenshotDirectory).getAbsolutePath();
        deviceLogsDirectory = FileUtils.createDirectoryIn(OsUtils.getUserDirectory(), deviceLogsDirectory).getAbsolutePath();
        testExecutionContext.addTestState(TEST_CONTEXT.EXAMPLE_RUN_COUNT, currentExampleRowNumberForScenario);
        testExecutionContext.addTestState(TEST_CONTEXT.SCENARIO_RUN_COUNT, runningScenarioNumber);
        testExecutionContext.addTestState(TEST_CONTEXT.NORMALISED_SCENARIO_NAME, normalisedScenarioName);
        testExecutionContext.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioLogDirectory);
        testExecutionContext.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY, screenshotDirectory);
        testExecutionContext.addTestState(TEST_CONTEXT.DEVICE_LOGS_DIRECTORY, deviceLogsDirectory);

    }

    private String normaliseScenarioName(String scenarioName) {
        return scenarioName.replaceAll("[`~ !@#$%^&*()\\-=+\\[\\]{}\\\\|;:'\",<.>/?]", "_");
    }

    private Integer updateCurrentExampleRowNumberForScenario(String scenarioName) {
        if (numberOfExamplesForScenario.containsKey(scenarioName)) {
            numberOfExamplesForScenario.put(scenarioName, numberOfExamplesForScenario.get(scenarioName) + 1);
        } else {
            numberOfExamplesForScenario.put(scenarioName, 1);
        }
        return numberOfExamplesForScenario.get(scenarioName);
    }

    private Integer getCurrentExampleRowNumberForScenario(String scenarioName) {
        return numberOfExamplesForScenario.get(scenarioName);
    }

    private void scenarioFinishedHandler(TestCaseFinished event) {
        String scenarioName = event.getTestCase().getName();
        Integer currentExampleRowNumberForScenario = getCurrentExampleRowNumberForScenario(scenarioName);

        LOGGER.info("Finished Scenario #" + runningScenarioNumber + "'" + scenarioName + "' started");
        LOGGER.info("\tCurrent Example Row Number: " + currentExampleRowNumberForScenario);

        long threadId = Thread.currentThread().getId();
        TestExecutionContext testExecutionContext = SessionContext.getTestExecutionContext(threadId);

        SessionContext.remove(threadId);
    }

    private void runFinishedHandler(TestRunFinished event) {
        LOGGER.info("runFinishedHandler: " + event.getResult().toString());
        LOGGER.info(String.format("ThreadID: %d: afterSuite: %n", Thread.currentThread().getId()));
        try {
            AppiumServerManager.destroyAppiumNode();
            SessionContext.setReportPortalLaunchURL();
        } catch (Exception e) {
            ExceptionUtils.getStackTrace(e);
        }
    }
}
