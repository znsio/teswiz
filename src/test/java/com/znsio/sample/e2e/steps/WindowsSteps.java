package com.znsio.sample.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Drivers;
import com.znsio.sample.e2e.businessLayer.notepad.NotepadBL;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.log4j.Logger;

public class WindowsSteps {
    private static final Logger LOGGER = Logger.getLogger(WindowsSteps.class.getName());
    private final TestExecutionContext context;
    private final Drivers allDrivers;

    public WindowsSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread()
                                                               .getId());
        LOGGER.info("context: " + context.getTestName());
        allDrivers = (Drivers) context.getTestState(SAMPLE_TEST_CONTEXT.ALL_DRIVERS);
        LOGGER.info("allDrivers: " + (null == allDrivers));
    }

    @Given("I have launched Notepad application")
    public void iHaveLaunchedNotepadApplication() {
        allDrivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.platform, context);
        LOGGER.info(System.out.printf("iHaveLaunchedNotepadApplication - Persona:'%s', Platform: '%s'", SAMPLE_TEST_CONTEXT.ME, Runner.platform));
        new NotepadBL(SAMPLE_TEST_CONTEXT.ME, Runner.platform).verifyLaunched();
    }

    @Then("I should be able to type {string}")
    public void iShouldBeAbleToType(String message) {
        LOGGER.info("iShouldBeAbleToType");
        new NotepadBL().typeMessage(message);
    }
}