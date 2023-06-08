package com.znsio.teswiz.listener;

import com.appium.filelocations.FileLocations;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class CucumberWebScenarioListener
        implements ConcurrentEventListener {
    private static final Logger LOGGER = Logger.getLogger(
            CucumberWebScenarioListener.class.getName());
    private final Map<String, Integer> scenarioRunCounts = new HashMap<>();

    public CucumberWebScenarioListener() {
        LOGGER.info(String.format("ThreadId: %d: CucumberWebScenarioListener%n",
                                  Thread.currentThread().getId()));
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunStarted.class, this::webRunStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::webCaseStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::webCaseFinishedHandler);
        eventPublisher.registerHandlerFor(TestRunFinished.class, this::webRunFinishedHandler);
    }

    private void webRunStartedHandler(TestRunStarted event) {
        LOGGER.info("webRunStartedHandler");
        LOGGER.info(String.format("ThreadId: %d: beforeSuite: %n", Thread.currentThread().getId()));
    }

    private void webCaseStartedHandler(TestCaseStarted event) {
        String scenarioName = event.getTestCase().getName();
        Integer scenarioRunCount = getScenarioRunCount(scenarioName);
        TestExecutionContext testExecutionContext = new TestExecutionContext(
                scenarioRunCount + "-" + scenarioName);

        LOGGER.info(String.format(
                "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   TEST-CASE  -- %s  " +
                "STARTED   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$",
                scenarioName));
        LOGGER.info(
                String.format("webCaseStartedHandler: '%s' with scenarioRunCount: %d", scenarioName,
                              scenarioRunCount));
        String normalisedScenarioName = normaliseScenarioName(scenarioName);

        LOGGER.info(String.format("ThreadId: %d: beforeScenario: for scenario: %s%n",
                                  Thread.currentThread().getId(), scenarioName));
        String scenarioLogDirectory = FileLocations.REPORTS_DIRECTORY + normalisedScenarioName;
        testExecutionContext.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY,
                                          scenarioLogDirectory);
        String screenshotDirectory =
                FileLocations.REPORTS_DIRECTORY + normalisedScenarioName + File.separator +
                "screenshot" + File.separator;
        testExecutionContext.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY, screenshotDirectory);
    }

    private void webCaseFinishedHandler(TestCaseFinished event) {
        String scenarioName = event.getTestCase().getName();
        LOGGER.info("webCaseFinishedHandler Name: " + scenarioName);
        LOGGER.info("webCaseFinishedHandler Result: " + event.getResult().getStatus().toString());
        long threadId = Thread.currentThread().getId();
        LOGGER.info(String.format("ThreadID: %d: afterScenario: for scenario: %s%n", threadId,
                                  scenarioName));
        SessionContext.remove(threadId);
        LOGGER.info(
                "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   TEST-CASE  -- " + scenarioName + "  ENDED   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    }

    private void webRunFinishedHandler(TestRunFinished event) {
        LOGGER.info("webRunFinishedHandler: " + event.getResult().toString());
        LOGGER.debug("webRunFinishedHandler: rp.launch.id: " + System.getProperty("rp.launch.id"));
        SessionContext.setReportPortalLaunchURL();
        LOGGER.info(String.format("ThreadId: %d: afterSuite: %n", Thread.currentThread().getId()));
    }

    private Integer getScenarioRunCount(String scenarioName) {
        if(scenarioRunCounts.containsKey(scenarioName)) {
            scenarioRunCounts.put(scenarioName, scenarioRunCounts.get(scenarioName) + 1);
        } else {
            scenarioRunCounts.put(scenarioName, 1);
        }
        return scenarioRunCounts.get(scenarioName);
    }

    private String normaliseScenarioName(String scenarioName) {
        return scenarioName.replaceAll("[`~ !@#$%^&*()\\-=+\\[\\]{}\\\\|;:'\",<.>/?]", "_");
    }
}
