package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.runner.Runner;
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
        long threadId = Thread.currentThread().getId();
        LOGGER.info(String.format("RunCukes: ThreadId: '%s': Constructor", threadId));
        context = SessionContext.getTestExecutionContext(threadId);
    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        LOGGER.info(String.format("RunCukes: ThreadId: %d: in overridden scenarios%n",
                                  Thread.currentThread().getId()));
        Object[][] scenarios = super.scenarios();
        LOGGER.info(scenarios);
        return scenarios;
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        LOGGER.info(String.format("RunCukes: ThreadId : %s In RunCukes - beforeScenario: %s", threadId,
                                  scenario.getName()));
        new Hooks().beforeScenario(scenario);
    }

    @After
    public void afterScenario(Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        LOGGER.info(String.format("RunCukes: ThreadId: %d In RunCukes - afterScenario: %s", threadId,
                                  scenario.getName()));
        new Hooks().afterScenario(scenario);
    }
}
