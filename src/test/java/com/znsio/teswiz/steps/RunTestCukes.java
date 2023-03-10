package com.znsio.teswiz.steps;

import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.selenium.Configuration;
import com.applitools.eyes.visualgrid.model.DeviceName;
import com.applitools.eyes.visualgrid.model.ScreenOrientation;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.APPLITOOLS;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import org.apache.log4j.Logger;
import org.testng.annotations.DataProvider;

public class RunTestCukes
        extends AbstractTestNGCucumberTests {
    private static final Logger LOGGER = Logger.getLogger(RunTestCukes.class.getName());
    private final TestExecutionContext context;

    public RunTestCukes() {
        long threadId = Thread.currentThread().getId();
        LOGGER.info("RunTestCukes: Constructor: ThreadId: " + threadId);
        context = SessionContext.getTestExecutionContext(threadId);
        System.setProperty(TEST_CONTEXT.TAGS_TO_EXCLUDE_FROM_CUCUMBER_REPORT, "@android,@web");
    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        LOGGER.info(String.format("RunTestCukes: ThreadId: %d: in overridden scenarios%n",
                                  Thread.currentThread().getId()));
        Object[][] scenarios = super.scenarios();
        LOGGER.info(scenarios);
        return scenarios;
    }

    @Before
    public void beforeTestScenario(Scenario scenario) {
        LOGGER.info(String.format("RunTestCukes: ThreadId: %d: in overridden beforeTestScenario%n",
                                  Thread.currentThread().getId()));
        new Hooks().beforeScenario(scenario);
        Configuration ufgConfig = new Configuration();
        ufgConfig.addBrowser(1024, 1024, BrowserType.CHROME);
        ufgConfig.addBrowser(1024, 1024, BrowserType.FIREFOX);
        ufgConfig.addDeviceEmulation(DeviceName.iPhone_X, ScreenOrientation.PORTRAIT);
        ufgConfig.addDeviceEmulation(DeviceName.OnePlus_7T_Pro, ScreenOrientation.LANDSCAPE);
        context.addTestState(APPLITOOLS.UFG_CONFIG, ufgConfig);
    }

    @After
    public void afterTestScenario(Scenario scenario) {
        LOGGER.info(String.format("RunTestCukes: ThreadId: %d: in overridden afterTestScenario%n",
                                  Thread.currentThread().getId()));
        new Hooks().afterScenario(scenario);
    }
}
