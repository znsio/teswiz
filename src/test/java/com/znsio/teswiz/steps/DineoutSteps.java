package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.dineout.DineoutSearchBL;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

public class DineoutSteps {
    private static final Logger LOGGER = Logger.getLogger(IndigoSteps.class.getName());
    private final TestExecutionContext context;

    public DineoutSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Given("I am in {string}")
    public void iAmIn(String city) {
        LOGGER.info(System.out.printf("iAmIn - Persona:'%s'", SAMPLE_TEST_CONTEXT.ME));
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new DineoutSearchBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).selectCity(city);
    }

    @When("I search for {string} cuisine restaurants")
    public void iSearchForCuisineRestaurants(String cusine) {
        LOGGER.info(System.out.printf("iSearchForCuisineRestaurants - Persona:'%s'",
                                      SAMPLE_TEST_CONTEXT.ME));
        new DineoutSearchBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).searchForCusine(cusine);
    }
}
