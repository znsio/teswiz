package com.znsio.teswiz.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;

import io.cucumber.java.en.When;

public class FigmaSteps {

    private static final Logger LOGGER = LogManager.getLogger(FigmaSteps.class.getName());
    private final TestExecutionContext context;

    public FigmaSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @When("I have my Figma design with app name {string}, test name {string} and baseline name {string} available in Applitools")
    public void iHaveMyFigmaDesignAvailableInApplitools(String appName, String testName, String baselineName) {
        LOGGER.info(String.format(
                "I have my Figma design with app name: '%s', test name: '%s', baseline name: '%s'",
                appName, testName, baselineName));
        addFigmaDesignDetailsToContext(appName, testName, baselineName);
        Drivers.createDriverFor(TEST_CONTEXT.I, Runner.getPlatform(), context);
    }

    void addFigmaDesignDetailsToContext(String appName, String testName, String baselineName) {
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_APP_NAME, appName);
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_TEST_NAME, testName);
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_BASELINE_ENV_NAME, baselineName);
    }
}
