package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.confengine.ConfEngineBL;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.Given;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ConfEngineSteps {
    private static final Logger LOGGER = LogManager.getLogger(ConfEngineSteps.class.getName());
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
