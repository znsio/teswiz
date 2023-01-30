package com.znsio.sample.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Drivers;
import com.znsio.sample.e2e.businessLayer.confengine.ConfEngineBL;
import com.znsio.sample.e2e.businessLayer.theapp.AppBL;
import com.znsio.sample.e2e.businessLayer.theapp.ClipboardBL;
import com.znsio.sample.e2e.businessLayer.theapp.EchoBL;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

public class ConfEngineSteps {
    private static final Logger LOGGER = Logger.getLogger(ConfEngineSteps.class.getName());
    private final TestExecutionContext context;
    private final Drivers allDrivers;

    public ConfEngineSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread()
                                                               .getId());
        LOGGER.info("context: " + context.getTestName());
        allDrivers = (Drivers) context.getTestState(SAMPLE_TEST_CONTEXT.ALL_DRIVERS);
        LOGGER.info("allDrivers: " + (null == allDrivers));
    }

    @Given("I see the list of conferences")
    public void iSeeTheListOfConferences() {
        LOGGER.info(System.out.printf("iSeeTheListOfConferences - Persona:'%s'", SAMPLE_TEST_CONTEXT.ME));
        allDrivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.platform, context);
        new ConfEngineBL(SAMPLE_TEST_CONTEXT.ME, Runner.platform).seeListOfConferences();
    }
}
