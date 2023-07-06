package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.autoscroll.AutoScrollBL;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.log4j.Logger;

public class AutoScrollSteps {
    private static final Logger LOGGER = Logger.getLogger(AutoScrollSteps.class.getName());
    private final TestExecutionContext context;

    public AutoScrollSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Then("I should be able to scroll in inner dropdown")
    public void iShouldBeAbleToScrollInInnerDropdown() {
        new AutoScrollBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).verifyScrollInDynamicLayerFunctionality();
    }

    @Given("on landing page, I see the list of available apps in a dropdown list")
    public void onLandingPageISeeTheListOfAvailableAppsInADropdownList() {
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new AutoScrollBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).goToDropdownWindow();
    }
}