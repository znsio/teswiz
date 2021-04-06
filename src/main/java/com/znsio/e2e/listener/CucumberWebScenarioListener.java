package com.znsio.e2e.listener;

import com.appium.filelocations.FileLocations;
import com.context.TestExecutionContext;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.runner.Runner;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CucumberWebScenarioListener implements ConcurrentEventListener {
    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());
    private final Map<String, Integer> scenarioRunCounts = new HashMap<String, Integer>();

    public CucumberWebScenarioListener () {
        LOGGER.info(String.format("ThreadID: %d: CucumberWebScenarioListener\n",
                Thread.currentThread().getId()));
    }

    @Override
    public void setEventPublisher (EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunStarted.class, this::webRunStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::webCaseStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::webCaseFinishedHandler);
        eventPublisher.registerHandlerFor(TestRunFinished.class, this::webRunFinishedHandler);
    }

    private void webRunStartedHandler (TestRunStarted event) {
        LOGGER.info("webRunStartedHandler");
        LOGGER.info(String.format("ThreadID: %d: beforeSuite: \n", Thread.currentThread().getId()));
    }

    private void webCaseStartedHandler (TestCaseStarted event) {
        String scenarioName = event.getTestCase().getName();
        LOGGER.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   Test case  -- "+ scenarioName +"  started   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        LOGGER.info("webCaseStartedHandler: " + scenarioName);
        Integer scenarioRunCount = getScenarioRunCount(scenarioName);
        LOGGER.info(
                String.format("ThreadID: %d: beforeScenario: for scenario: %s\n",
                        Thread.currentThread().getId(), scenarioName));
        String normalisedScenarioName = normaliseScenarioName(scenarioName);
        TestExecutionContext testExecutionContext = new TestExecutionContext(scenarioName);
        testExecutionContext.addTestState(TEST_CONTEXT.DEVICE_INFO,
                "Chrome browser - version: " + WebDriverManager.chromedriver().getDownloadedDriverVersion());
        testExecutionContext.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, FileLocations.REPORTS_DIRECTORY
                + normalisedScenarioName);
        testExecutionContext.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY,
                FileLocations.REPORTS_DIRECTORY
                        + normalisedScenarioName
                        + File.separator
                        + "screenshot"
                        + File.separator);
        String testLogFileName= FileLocations.REPORTS_DIRECTORY
                + normalisedScenarioName
                + File.separator
                + FileLocations.TEST_LOGS_DIRECTORY
                + "/run-"+ scenarioRunCount;

        testExecutionContext.addTestState("testLog", testLogFileName);
        System.setProperty("log_dir", testLogFileName);
    }

    private void webCaseFinishedHandler (TestCaseFinished event) {
        String scenarioName = event.getTestCase().getName();
        LOGGER.info("webCaseFinishedHandler Name: " + scenarioName);
        LOGGER.info("webCaseFinishedHandler Result: " + event.getResult().getStatus().toString());
        long threadId = Thread.currentThread().getId();
        LOGGER.info(
                String.format("ThreadID: %d: afterScenario: for scenario: %s\n",
                        threadId, scenarioName));
        Runner.remove(threadId);
        LOGGER.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   Test case  -- "+ scenarioName +"  started   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    }

    private void webRunFinishedHandler (TestRunFinished event) {
        LOGGER.info("webRunFinishedHandler: " + event.getResult().toString());
        LOGGER.info(String.format("ThreadID: %d: afterSuite: \n", Thread.currentThread().getId()));
    }

    private Integer getScenarioRunCount (String scenarioName) {
        if (scenarioRunCounts.containsKey(scenarioName)) {
            scenarioRunCounts.put(scenarioName, scenarioRunCounts.get(scenarioName) + 1);
        } else {
            scenarioRunCounts.put(scenarioName, 1);
        }
        return scenarioRunCounts.get(scenarioName);
    }

    private String normaliseScenarioName (String scenarioName) {
        return scenarioName.replaceAll("[`~ !@#$%^&*()\\-=+\\[\\]{}\\\\|;:'\",<.>/?]", "_");
    }
}
