package com.znsio.sample.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.runner.Drivers;
import com.znsio.sample.e2e.businessLayer.calculator.CalculatorBL;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

import static com.znsio.e2e.tools.Wait.waitFor;

public class CalculatorSteps {
    private static final Logger LOGGER = Logger.getLogger(CalculatorSteps.class.getName());
    private final TestExecutionContext context;

    public CalculatorSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Given("I select {string}")
    public void iSelect(String number) {
        new CalculatorBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).selectNumber(number);
    }

    @And("{string} select {string}")
    public void select(String userPersona, String action) {
        Platform onPlatform = Drivers.getPlatformForUser(userPersona);
        new CalculatorBL(userPersona, onPlatform).startCalculator().selectNumber(action);
    }

    @When("I press {string}")
    public void iPress(String operation) {
        new CalculatorBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).pressOperation(operation);
    }

    @Then("I should see {string}")
    public void iShouldSee(String finalResult) {
    }

    @Given("I start the calculator")
    public void iStartTheCalculator() {
        LOGGER.info(
                System.out.printf("iStartTheCalculator - Persona:'%s'", SAMPLE_TEST_CONTEXT.ME));
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new CalculatorBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).startCalculator();
    }

    @And("{string} press {string}")
    public void press(String userPersona, String action) {
        Platform onPlatform = Drivers.getPlatformForUser(userPersona);
        new CalculatorBL(userPersona, onPlatform).pressOperation(action);
        if(action.equalsIgnoreCase("equals")) {
            waitFor(10);
        }
    }
}
