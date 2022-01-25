package com.znsio.e2e.steps;

import com.context.*;
import com.znsio.e2e.businessLayer.*;
import com.znsio.e2e.entities.*;
import com.znsio.e2e.runner.*;
import com.znsio.e2e.tools.*;
import io.cucumber.java.en.*;
import org.apache.log4j.*;

public class WindowsSteps {
    private static final Logger LOGGER = Logger.getLogger(WindowsSteps.class.getName());
    private final TestExecutionContext context;
    private final Drivers allDrivers;

    public WindowsSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
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