package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.vodqa.VodqaBL;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
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

    @When("I tap in the middle of the screen")
    public void iTapInTheMiddleOfTheScreen() {
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).tapInTheMiddleOfTheScreen();
    }

    @Then("I am able to move from {string} page to next page")
    public void iAmAbleToMoveFromPageToNextPage(String pageHeading) {
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).verifyUserMoveToNextPage(pageHeading);
    }
  
    @When("I scroll down by screen size on vertical swiping screen")
    public void iScrollDownByScreenSizeOnVerticalSwipingScreen() {
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).scrollDownByScreenSizeOnVerticalSwipingScreen();
    }

    @When("I swipe left on {string} screen")
    public void selectScreenAndSwipeLeft(String screenName) {
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).selectScreenAndSwipeLeft(screenName);
    }

    @When("I swipe right on {string} screen")
    public void selectScreenAndSwipeRight(String screenName) {
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).selectScreenAndSwipeRight(screenName);
    }

    @Then("I am able to see element with text {string} on the screen")
    public void iAmAbleToSeeElementWithTextOnTheScreen(String elementText) {
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).verifySwipe(elementText);
    }

    @When("I swipe at {int} percent height from {int} percent width to {int} percent width on {string} screen")
    public void iSwipeAtPercentHeightFromPercentWidthToPercentWidthOnScreen(int atPercentileHeight, int fromPercentageWidth, int toPercentageWidth, String screenName) {
        new VodqaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform())
                .selectScreenAndSwipeByPassingPercentageAttributes(atPercentileHeight, fromPercentageWidth, toPercentageWidth, screenName);
    }
}
