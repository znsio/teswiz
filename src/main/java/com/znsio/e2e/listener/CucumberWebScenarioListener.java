package com.znsio.e2e.listener;

import com.appium.filelocations.FileLocations;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.runner.Runner;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class CucumberWebScenarioListener
        implements ConcurrentEventListener {
    private static final Logger LOGGER = Logger.getLogger(CucumberWebScenarioListener.class.getName());
    private final Map<String, Integer> scenarioRunCounts = new HashMap<>();

    public CucumberWebScenarioListener() {
        LOGGER.info(String.format("ThreadID: %d: CucumberWebScenarioListener\n", Thread.currentThread()
                                                                                       .getId()));
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
        LOGGER.info(String.format("ThreadID: %d: beforeSuite: \n", Thread.currentThread()
                                                                         .getId()));
    }

    private void webCaseStartedHandler(TestCaseStarted event) {
        String scenarioName = event.getTestCase()
                                   .getName();
        TestExecutionContext testExecutionContext = new TestExecutionContext(scenarioName);

        LOGGER.info(
                "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   TEST-CASE  -- " + scenarioName + "  STARTED   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        LOGGER.info("webCaseStartedHandler: " + scenarioName);
        Integer scenarioRunCount = getScenarioRunCount(scenarioName);
        String normalisedScenarioName = normaliseScenarioName(scenarioName);

        LOGGER.info(String.format("ThreadID: %d: beforeScenario: for scenario: %s\n", Thread.currentThread()
                                                                                            .getId(), scenarioName));
        testExecutionContext.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, FileLocations.REPORTS_DIRECTORY + normalisedScenarioName);
        testExecutionContext.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY,
                                          FileLocations.REPORTS_DIRECTORY + normalisedScenarioName + File.separator + "screenshot" + File.separator);
    }

    private void webCaseFinishedHandler(TestCaseFinished event) {
        String scenarioName = event.getTestCase()
                                   .getName();
        LOGGER.info("webCaseFinishedHandler Name: " + scenarioName);
        LOGGER.info("webCaseFinishedHandler Result: " + event.getResult()
                                                             .getStatus()
                                                             .toString());
        long threadId = Thread.currentThread()
                              .getId();
        LOGGER.info(String.format("ThreadID: %d: afterScenario: for scenario: %s\n", threadId, scenarioName));
        Runner.remove(threadId);
        LOGGER.info(
                "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   TEST-CASE  -- " + scenarioName + "  ENDED   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    }

    private void webRunFinishedHandler(TestRunFinished event) {
        LOGGER.info("webRunFinishedHandler: " + event.getResult()
                                                     .toString());
        SessionContext.setReportPortalLaunchURL();
        LOGGER.info(String.format("ThreadID: %d: afterSuite: \n", Thread.currentThread()
                                                                        .getId()));
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
