package com.znsio.e2e.steps;

import com.context.*;
import com.epam.reportportal.service.*;
import com.znsio.e2e.entities.*;
import com.znsio.e2e.runner.*;
import com.znsio.e2e.tools.*;
import io.cucumber.java.*;
import org.apache.log4j.*;
import org.assertj.core.api.*;

import java.util.*;

public class Hooks {
    private static final Logger LOGGER = Logger.getLogger(Hooks.class.getName());
    private static final String DEBUG = "debug";
    private static final String INFO = "info";

    public void beforeScenario(Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        TestExecutionContext testExecutionContext = Runner.getTestExecutionContext(threadId);
        LOGGER.info(String.format("ThreadId : %s In RunCukes - Before: %s", threadId, scenario.getName()));
        LOGGER.info(String.format("Running test %s on %s", testExecutionContext.getTestName(), Runner.platform.name()));
        testExecutionContext.addTestState(TEST_CONTEXT.SCREENSHOT_MANAGER, new ScreenShotManager());
        testExecutionContext.addTestState(TEST_CONTEXT.ALL_DRIVERS, new Drivers());
        SoftAssertions softly = new SoftAssertions();
        testExecutionContext.addTestState(TEST_CONTEXT.SOFT_ASSERTIONS, softly);
        addEnvironmentVariablesToReportPortal();
        addSystemPropertiesToReportPortal();
    }

    private void addSystemPropertiesToReportPortal() {
        Properties props = System.getProperties();
        final String[] propVars = {""};
        props.forEach((k, v) -> propVars[0] += ("\t" + k + ":" + v + "\n"));
        ReportPortal.emitLog("System Properties:\n" + propVars[0], DEBUG, new Date());
    }

    private void addEnvironmentVariablesToReportPortal() {
        Map<String, String> env = System.getenv();
        final String[] envVars = {""};
        env.forEach((k, v) -> envVars[0] +=("\t" + k + ":" + v + "\n"));
        ReportPortal.emitLog("Environment Variables:\n" + envVars[0], DEBUG, new Date());
    }

    public void afterScenario(Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        LOGGER.info("ThreadId: " + threadId + " In RunCukes - After: " + scenario.getName());
        Runner.closeAllDrivers(threadId);
        SoftAssertions softly = Runner.getSoftAssertion(threadId);
        softly.assertAll();
    }
}
