package com.znsio.e2e.steps;

import com.context.*;
import com.znsio.e2e.businessLayer.*;
import com.znsio.e2e.entities.*;
import com.znsio.e2e.runner.*;
import com.znsio.e2e.tools.*;
import io.cucumber.java.en.*;
import org.apache.log4j.*;

public class CalculatorSteps {
    private static final Logger LOGGER = Logger.getLogger(CalculatorSteps.class.getName());
    private final TestExecutionContext context;
    private final Drivers allDrivers;

    public CalculatorSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
        allDrivers = (Drivers) context.getTestState(SAMPLE_TEST_CONTEXT.ALL_DRIVERS);
        LOGGER.info("allDrivers: " + (null == allDrivers));
    }

    @Given("I select {string}")
    public void iSelect(String number) {
        new CalculatorBL(SAMPLE_TEST_CONTEXT.ME, Runner.platform).selectNumber(number);
    }

    @When("I press {string}")
    public void iPress(String operation) {
        new CalculatorBL(SAMPLE_TEST_CONTEXT.ME, Runner.platform).pressOperation(operation);
    }

    @Then("I should see {string}")
    public void iShouldSee(String finalResult) {
    }

    @Given("I start the calculator")
    public void iStartTheCalculator() {
        LOGGER.info(System.out.printf("iStartTheCalculator - Persona:'%s'", SAMPLE_TEST_CONTEXT.ME));
        allDrivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.platform, context);
        new CalculatorBL(SAMPLE_TEST_CONTEXT.ME, Runner.platform).startCalculator();
    }

    @And("{string} select {string}")
    public void select(String userPersona, String action) {
        Platform onPlatform = allDrivers.getPlatformForUser(userPersona);
        new CalculatorBL(userPersona, onPlatform).startCalculator().selectNumber(action);
    }

    @And("{string} press {string}")
    public void press(String userPersona, String action) {
        Platform onPlatform = allDrivers.getPlatformForUser(userPersona);
        new CalculatorBL(userPersona, onPlatform).pressOperation(action);
    }
}
