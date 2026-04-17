package com.znsio.teswiz.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;

import io.cucumber.java.en.When;

public class CommonSteps {

    private static final Logger LOGGER = LogManager.getLogger(CommonSteps.class.getName());
    private final TestExecutionContext testExecutionContext;

    public CommonSteps() {
        testExecutionContext = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + testExecutionContext.getTestName());
    }

    @When("I visually check the {string} at {string}")
    public void iVisuallyCheck(String pageName, String url) {
            LOGGER.info(System.out.printf("iVisuallyCheck page: '%s', url: '%s'", pageName, url));
            Driver driver = Drivers.getDriverForCurrentUser(Thread.currentThread().getId());
            driver.getInnerDriver().get(url);
            driver.getVisual().checkWindow(pageName, url);
    }
    
    @When("I go to the {string} at {string}")
    public void iGoTo(String pageName, String url) {
        LOGGER.info(System.out.printf("iGoTo page: '%s', url: '%s'", pageName, url));
        Driver driver = Drivers.getDriverForCurrentUser(Thread.currentThread().getId());
        driver.getInnerDriver().get(url);
    }
    
    @When("I go the application page")
    public void iGoToApplicationPage() {
        LOGGER.info("iGoToApplicationPage");
        Drivers.createDriverFor(TEST_CONTEXT.I, Runner.getPlatform(), testExecutionContext);
    }
}
