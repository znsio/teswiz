package com.znsio.e2e.steps;

import com.applitools.eyes.selenium.*;
import com.applitools.eyes.visualgrid.model.*;
import com.context.*;
import com.znsio.e2e.entities.*;
import io.cucumber.java.*;
import io.cucumber.testng.*;
import org.apache.log4j.*;
import org.testng.annotations.*;

import java.util.*;

public class RunTestCukes extends AbstractTestNGCucumberTests {
    private static final Logger LOGGER = Logger.getLogger(RunTestCukes.class.getName());
    private final TestExecutionContext context;

    public RunTestCukes() {
        long threadId = Thread.currentThread().getId();
        LOGGER.info("RunTestCukes constructor: ThreadId: " + threadId);
        context = SessionContext.getTestExecutionContext(threadId);
    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios () {
        LOGGER.info(String.format("ThreadID: %d: in overridden scenarios%n", Thread.currentThread().getId()));
        Object[][] scenarios = super.scenarios();
        LOGGER.info(scenarios);
        return scenarios;
    }

    @Before
    public void beforeTestScenario (Scenario scenario) {
        LOGGER.info(String.format("ThreadID: %d: in overridden beforeTestScenario%n", Thread.currentThread().getId()));
//        Configuration ufgConfig = new Configuration();
//        ufgConfig.addBrowser(1024, 1024, BrowserType.CHROME);
//        ufgConfig.addBrowser(1024, 1024, BrowserType.FIREFOX);
//        ufgConfig.addBrowser(1024, 1024, BrowserType.SAFARI);
//        ufgConfig.addBrowser(1024, 1024, BrowserType.EDGE_CHROMIUM);
//        ufgConfig.addBrowser(1600, 1200, BrowserType.CHROME);
//        ufgConfig.addBrowser(1600, 1200, BrowserType.FIREFOX);
//        ufgConfig.addBrowser(1600, 1200, BrowserType.SAFARI);
//        ufgConfig.addBrowser(1600, 1200, BrowserType.EDGE_CHROMIUM);
//        ufgConfig.addDeviceEmulation(DeviceName.iPhone_X, ScreenOrientation.PORTRAIT);
//        ufgConfig.addDeviceEmulation(DeviceName.iPad_Pro, ScreenOrientation.LANDSCAPE);
//        ufgConfig.addDeviceEmulation(DeviceName.Nexus_5X, ScreenOrientation.PORTRAIT);
//        ufgConfig.addDeviceEmulation(DeviceName.Nexus_6P, ScreenOrientation.LANDSCAPE);
//        context.addTestState(APPLITOOLS.UFG_CONFIG, ufgConfig);
    }

    @After
    public void afterTestScenario (Scenario scenario) {
        LOGGER.info(String.format("ThreadID: %d: in overridden afterTestScenario%n", Thread.currentThread().getId()));
    }
}
