package com.znsio.teswiz.listener;

import com.appium.filelocations.FileLocations;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Runner;
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


public class CucumberScenarioListener
        implements ConcurrentEventListener {
    private static final Logger LOGGER = Logger.getLogger(
            CucumberScenarioListener.class.getName());
    private final Map<String, Integer> scenarioRunCounts = new HashMap<>();

    public CucumberScenarioListener() {
        LOGGER.info(String.format("ThreadId: %d: CucumberScenarioListener%n",
                                  Thread.currentThread().getId()));
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunStarted.class, this::testRunStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::testCaseStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::testCaseFinishedHandler);
        eventPublisher.registerHandlerFor(TestRunFinished.class, this::testRunFinishedHandler);
    }

    private void testRunStartedHandler(TestRunStarted event) {
        LOGGER.info("testRunStartedHandler for platform :" + Runner.getPlatform());
        LOGGER.info(String.format("ThreadId: %d: beforeSuite: %n", Thread.currentThread().getId()));
    }

    private void testCaseStartedHandler(TestCaseStarted event) {
        String scenarioName = event.getTestCase().getName();
        Integer scenarioRunCount = getScenarioRunCount(scenarioName);
        TestExecutionContext testExecutionContext = new TestExecutionContext(
                scenarioRunCount + "-" + scenarioName);

        LOGGER.info(String.format(
                "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   TEST-CASE  -- %s  " +
                "STARTED   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$",
                scenarioName));
        LOGGER.info(
                String.format("testCaseStartedHandler: '%s' with scenarioRunCount: %d and platform: %s", scenarioName,
                              scenarioRunCount, Runner.getPlatform()));
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

    private void testCaseFinishedHandler(TestCaseFinished event) {
        String scenarioName = event.getTestCase().getName();
        LOGGER.info("testCaseFinishedHandler Name: " + scenarioName +" ,Platform: "+Runner.getPlatform());
        LOGGER.info("testCaseFinishedHandler Result: " + event.getResult().getStatus().toString());
        long threadId = Thread.currentThread().getId();
        LOGGER.info(String.format("ThreadID: %d: afterScenario: for scenario: %s%n", threadId,
                                  scenarioName));
        SessionContext.remove(threadId);
        LOGGER.info(
                "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   TEST-CASE  -- " + scenarioName + "  ENDED   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    }

    private void testRunFinishedHandler(TestRunFinished event) {
        LOGGER.info("testRunFinishedHandler: " + event.getResult().toString()+" ,Platform: "+Runner.getPlatform());
        LOGGER.debug("testRunFinishedHandler: rp.launch.id: " + System.getProperty("rp.launch.id"));
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