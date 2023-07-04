package com.znsio.teswiz.listener;

import com.appium.filelocations.FileLocations;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;


public class CucumberApiScenarioListener
        implements ConcurrentEventListener {
    private static final Logger LOGGER = Logger.getLogger(
            CucumberApiScenarioListener.class.getName());
    private final Map<String, Integer> scenarioRunCounts = new HashMap<>();

    public CucumberApiScenarioListener() {
        LOGGER.info(String.format("ThreadId: %d: CucumberApiScenarioListener%n",
                                  Thread.currentThread().getId()));
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunStarted.class, this::apiRunStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::apiCaseStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::apiCaseFinishedHandler);
        eventPublisher.registerHandlerFor(TestRunFinished.class, this::apiRunFinishedHandler);
    }

    private void apiRunStartedHandler(TestRunStarted event) {
        LOGGER.info("apiRunStartedHandler");
        LOGGER.info(String.format("ThreadId: %d: beforeSuite: %n", Thread.currentThread().getId()));
    }

    private void apiCaseStartedHandler(TestCaseStarted event) {
        String scenarioName = event.getTestCase().getName();
        Integer scenarioRunCount = getScenarioRunCount(scenarioName);
        TestExecutionContext testExecutionContext = new TestExecutionContext(
                scenarioRunCount + "-" + scenarioName);

        LOGGER.info(String.format(
                "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   TEST-CASE  -- %s  " +
                "STARTED   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$",
                scenarioName));
        LOGGER.info(
                String.format("apiCaseStartedHandler: '%s' with scenarioRunCount: %d", scenarioName,
                              scenarioRunCount));
        String normalisedScenarioName = normaliseScenarioName(scenarioName);

        LOGGER.info(String.format("ThreadId: %d: beforeScenario: for scenario: %s%n",
                                  Thread.currentThread().getId(), scenarioName));
        String scenarioLogDirectory = FileLocations.REPORTS_DIRECTORY + normalisedScenarioName;
        testExecutionContext.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY,
                                          scenarioLogDirectory);
    }

    private void apiCaseFinishedHandler(TestCaseFinished event) {
        String scenarioName = event.getTestCase().getName();
        LOGGER.info("apiCaseFinishedHandler Name: " + scenarioName);
        LOGGER.info("apiCaseFinishedHandler Result: " + event.getResult().getStatus().toString());
        long threadId = Thread.currentThread().getId();
        LOGGER.info(String.format("ThreadID: %d: afterScenario: for scenario: %s%n", threadId,
                                  scenarioName));
        SessionContext.remove(threadId);
        LOGGER.info(
                "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   TEST-CASE  -- " + scenarioName + "  ENDED   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    }

    private void apiRunFinishedHandler(TestRunFinished event) {
        LOGGER.info("apiRunFinishedHandler: " + event.getResult().toString());
        LOGGER.debug("apiRunFinishedHandler: rp.launch.id: " + System.getProperty("rp.launch.id"));
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
