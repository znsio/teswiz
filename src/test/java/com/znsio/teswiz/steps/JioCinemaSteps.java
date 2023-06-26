package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.jiocinema.JioCinemaBL;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

public class JioCinemaSteps {
    private static final Logger LOGGER = Logger.getLogger(JioCinemaSteps.class.getName());
    private final TestExecutionContext context;

    public JioCinemaSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Given("I navigate to jio Cinema application's home page")
    public void iNavigateToJioCinemaApplication() {
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new JioCinemaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).scrollTillTrendingInIndiaSection();
    }

    @When("I swipe right on tending in india section")
    public void iSwipeRightOnTrendingInIndiaSection() {
        new JioCinemaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).swipeRightOnTrendingInIndiaSection();
    }

    @When("I swipe left on tending in india section")
    public void iSwipeLeftOnTrendingInIndiaSection() {
        new JioCinemaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).swipeLeftOnTrendingInIndiaSection();
    }

    @Then("I am able to view number {int} trending movie")
    public void iAmAbleToViewNumberTrendingCinema(int movieNumberOnScreen) {
        new JioCinemaBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).verifyMovieNumberVisibleOnScreen(movieNumberOnScreen);
    }
}