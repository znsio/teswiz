package com.znsio.e2e.steps;

import com.context.TestExecutionContext;
import com.epam.reportportal.service.ReportPortal;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Drivers;
import com.znsio.e2e.tools.ScreenShotManager;
import io.cucumber.java.Scenario;
import org.assertj.core.api.SoftAssertions;

import java.util.Date;

public class Hooks {
    public void beforeScenario (Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        TestExecutionContext testExecutionContext = Runner.getTestExecutionContext(threadId);
        System.out.printf("ThreadId: '%d': In RunCukes - Before: '%s'%n", threadId, scenario.getName());
        System.out.printf("Running test: '%s' on '%s'%n", testExecutionContext.getTestName(), Runner.platform);
        testExecutionContext.addTestState(TEST_CONTEXT.SCREENSHOT_MANAGER, new ScreenShotManager());
        testExecutionContext.addTestState(TEST_CONTEXT.ALL_DRIVERS, new Drivers());
        SoftAssertions softly = new SoftAssertions();
        testExecutionContext.addTestState(TEST_CONTEXT.SOFT_ASSERTIONS, softly);
        ReportPortal.emitLog(testExecutionContext.getTestState(TEST_CONTEXT.DEVICE_INFO).toString(), "info", new Date());
    }

    public void afterScenario (Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        System.out.printf("ThreadId: '%d': In RunCukes - After: '%s'%n", threadId, scenario.getName());
        TestExecutionContext testExecutionContext = Runner.getTestExecutionContext(threadId);
        ScreenShotManager screenShotManager = (ScreenShotManager) testExecutionContext.getTestState(TEST_CONTEXT.SCREENSHOT_MANAGER);
        takeScreenShotOnTestCompletion(scenario, screenShotManager);
        Runner.closeAllDrivers(threadId);
        SoftAssertions softly = Runner.getSoftAssertion(threadId);
        softly.assertAll();
    }

    private void takeScreenShotOnTestCompletion (Scenario scenario, ScreenShotManager screenShotManager) {
        if (scenario.isFailed()) {
            screenShotManager.takeScreenShot(scenario.getName() + "-AfterTest");
        }
    }
}
