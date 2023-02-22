package com.znsio.sample.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Drivers;
import com.znsio.sample.e2e.businessLayer.dineout.DineoutSearchBL;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

public class DineoutSteps {
    private static final Logger LOGGER = Logger.getLogger(IndigoSteps.class.getName());
    private final TestExecutionContext context;
    private final Drivers allDrivers;

    public DineoutSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
        allDrivers = (Drivers) context.getTestState(SAMPLE_TEST_CONTEXT.ALL_DRIVERS);
        LOGGER.info("allDrivers: " + (null == allDrivers));
    }

    @Given("I am in {string}")
    public void iAmIn(String city) {
        LOGGER.info(System.out.printf("iAmIn - Persona:'%s'", SAMPLE_TEST_CONTEXT.ME));
        allDrivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.platform, context);
        new DineoutSearchBL(SAMPLE_TEST_CONTEXT.ME, Runner.platform).selectCity(city);
    }

    @When("I search for {string} cuisine restaurants")
    public void iSearchForCuisineRestaurants(String cusine) {
        LOGGER.info(System.out.printf("iSearchForCuisineRestaurants - Persona:'%s'",
                                      SAMPLE_TEST_CONTEXT.ME));
        new DineoutSearchBL(SAMPLE_TEST_CONTEXT.ME, Runner.platform).searchForCusine(cusine);
    }
}
