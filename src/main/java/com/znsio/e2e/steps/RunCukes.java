package com.znsio.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import org.apache.log4j.Logger;
import org.testng.annotations.DataProvider;

public class RunCukes
        extends AbstractTestNGCucumberTests {
    private static final Logger LOGGER = Logger.getLogger(RunCukes.class.getName());
    private final TestExecutionContext context;

    public RunCukes() {
        long threadId = Thread.currentThread()
                              .getId();
        LOGGER.info("RunCukes constructor: ThreadId: " + threadId);
        context = SessionContext.getTestExecutionContext(threadId);
    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        LOGGER.info(String.format("ThreadID: %d: in overridden scenarios%n", Thread.currentThread()
                                                                                   .getId()));
        Object[][] scenarios = super.scenarios();
        LOGGER.info(scenarios);
        return scenarios;
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        new Hooks().beforeScenario(scenario);
    }

    @After
    public void afterScenario(Scenario scenario) {
        new Hooks().afterScenario(scenario);
    }
}
