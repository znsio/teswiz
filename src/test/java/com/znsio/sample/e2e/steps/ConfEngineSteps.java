package com.znsio.sample.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.runner.Drivers;
import com.znsio.sample.e2e.businessLayer.confengine.ConfEngineBL;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.Given;
import org.apache.log4j.Logger;

public class ConfEngineSteps {
    private static final Logger LOGGER = Logger.getLogger(ConfEngineSteps.class.getName());
    private final TestExecutionContext context;

    public ConfEngineSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Given("I see the list of conferences")
    public void iSeeTheListOfConferences() {
        LOGGER.info(System.out.printf("iSeeTheListOfConferences - Persona:'%s'",
                                      SAMPLE_TEST_CONTEXT.ME));
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new ConfEngineBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).seeListOfConferences();
    }
}
