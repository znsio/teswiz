package com.znsio.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.epam.reportportal.service.ReportPortal;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Drivers;
import com.znsio.e2e.tools.ScreenShotManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.DataProvider;

import java.util.Date;

public class RunCukes extends AbstractTestNGCucumberTests {
    private final TestExecutionContext context;
    private final ScreenShotManager screenShotManager;

    public RunCukes () {
        long threadId = Thread.currentThread().getId();
        System.out.println("RunCukes constructor: ThreadId: " + threadId);
        context = SessionContext.getTestExecutionContext(threadId);
        screenShotManager = new ScreenShotManager();
    }

//    public void run (ArrayList<String> args, String stepDefsDir, String featuresDir) {
//        args.add("--glue");
//        args.add(stepDefsDir);
//        args.add(featuresDir);
//        System.out.println("Begin running tests...");
//        System.out.println("Args: " + args);
//        String[] array = args.stream().toArray(String[]::new);
//        Main.main(array);
//    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios () {
        System.out.printf("ThreadID: %d: in overridden scenarios%n", Thread.currentThread().getId());
        Object[][] scenarios = super.scenarios();
        System.out.println(scenarios);
        return scenarios;
    }

    @Before
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

    @After
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
