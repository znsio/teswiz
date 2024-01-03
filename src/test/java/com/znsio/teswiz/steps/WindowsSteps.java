package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.notepad.NotepadBL;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class WindowsSteps {
    private static final Logger LOGGER = LogManager.getLogger(WindowsSteps.class.getName());
    private final TestExecutionContext context;

    public WindowsSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Given("I have launched Notepad application")
    public void iHaveLaunchedNotepadApplication() {
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        LOGGER.info(
                System.out.printf("iHaveLaunchedNotepadApplication - Persona:'%s', Platform: '%s'",
                                  SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()));
        new NotepadBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).verifyLaunched();
    }

    @Then("I should be able to type {string}")
    public void iShouldBeAbleToType(String message) {
        LOGGER.info("iShouldBeAbleToType");
        new NotepadBL().typeMessage(message);
    }
}