package com.znsio.sample.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Drivers;
import com.znsio.sample.e2e.businessLayer.Amazon.*;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

public class AmazonSteps {
    private static final Logger LOGGER = Logger.getLogger(AmazonSteps.class.getName());
    private final TestExecutionContext context;
    private final Drivers allDrivers;

    public AmazonSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("Context: " + context.getTestName());

        allDrivers = (Drivers) context.getTestState(SAMPLE_TEST_CONTEXT.ALL_DRIVERS);
        LOGGER.info("allDrivers: " + (null == allDrivers));
    }

    @Given("I logged in to Amazon with valid credentials")
    public void iLoggedInToAmazonWithValidCredentials() {
        LOGGER.info("Setting up Driver");
        allDrivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.platform, context);

        LOGGER.info("Login to Amazon with Valid credentials");
        new LoginBL().loginToAmazon();
    }

    @When("I searched for {string} and select first result")
    public void iSearchedForAndSelectFirstResult(String itemName) {
        LOGGER.info("Searching and selecting item");
        new HomeBL().searchAndNavigateToProductDetail(itemName);
    }

    @And("I add {string} to the cart")
    public void iAddToTheCart(String itemName) {
        LOGGER.info("Adding product to the cart");
        new ProductBL().addAndNavigateToCart(itemName);
    }

    @Then("Cart should have added item {string} in it")
    public void cartShouldHaveAddedItemInIt(String itemName) {
        LOGGER.info("Verifying Item present in Cart");
        new CartBL().isProductExistInCart(itemName);
    }
}
