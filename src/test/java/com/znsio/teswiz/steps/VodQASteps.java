package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.vodqa.VodqaBL;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

public class VodQASteps {
    private static final Logger LOGGER = Logger.getLogger(VodQASteps.class.getName());
    private final TestExecutionContext context;

    public VodQASteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Given("I login to vodqa application using valid credentials")
    public void loginToApplication() {
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).login();
    }

    @When("I scroll from one to another element point on vertical swiping screen")
    public void scrollToElement() {
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).scrollFromOneElementPointToAnother();
    }

    @Then("Element text should be visible")
    public void isElementWithTextVisible() {
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).isElementWithTextVisible();
    }

    @When("I swipe left on {string} screen")
    public void iSwipeLeftOnScreen(String screenName) {
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).selectAScreenAndSwipeLeft(screenName);
    }

    @Then("I see tile element {string} on the screen")
    public void iSeeTileElementOnTheScreen(String tileNumber) {
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).verifySwipe(tileNumber);
    }

    @When("I swipe right on {string} screen")
    public void iSwipeRightOnScreen(String screenName) {
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).selectAScreenAndSwipeRight(screenName);
    }

    @When("I swipe at percentage height {int} from percentage width {int} to percentage width {int} on {string} screen")
    public void iSwipeAtPercentageHeightFromPercentageWidthToPercentageWidthOnScreen(int atPercentileHeight, int fromPercentageWidth, int toPercentageWidth, String screenName) {
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform())
                .selectAScreenAndSwipeByPassingPercentageAttributes(atPercentileHeight, fromPercentageWidth, toPercentageWidth, screenName);
    }
}
