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

    public void beforeScenario(Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        TestExecutionContext testExecutionContext = Runner.getTestExecutionContext(threadId);
        LOGGER.info("ThreadId :  " + threadId + " In RunCukes - Before:  " + scenario.getName());
        LOGGER.info("Running test  " + testExecutionContext.getTestName() + " on  " + Runner.platform);
        testExecutionContext.addTestState(TEST_CONTEXT.SCREENSHOT_MANAGER, new ScreenShotManager());
        testExecutionContext.addTestState(TEST_CONTEXT.ALL_DRIVERS, new Drivers());
        SoftAssertions softly = new SoftAssertions();
        testExecutionContext.addTestState(TEST_CONTEXT.SOFT_ASSERTIONS, softly);
        ReportPortal.emitLog(testExecutionContext.getTestState(TEST_CONTEXT.DEVICE_INFO).toString(), "info", new Date());
    }

    public void afterScenario(Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        LOGGER.info("ThreadId:  " + threadId + "  In RunCukes - After: " + scenario.getName());
        TestExecutionContext testExecutionContext = Runner.getTestExecutionContext(threadId);
        ScreenShotManager screenShotManager = (ScreenShotManager) testExecutionContext.getTestState(TEST_CONTEXT.SCREENSHOT_MANAGER);
        takeScreenShotOnTestCompletion(scenario, screenShotManager);
        Runner.closeAllDrivers(threadId);
        SoftAssertions softly = Runner.getSoftAssertion(threadId);
        softly.assertAll();
    }

    private void takeScreenShotOnTestCompletion(Scenario scenario, ScreenShotManager screenShotManager) {
        if (scenario.isFailed()) {
            screenShotManager.takeScreenShot(scenario.getName() + "-AfterTest");
        }
    }
}
