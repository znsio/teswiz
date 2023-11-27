package com.znsio.teswiz.steps;

import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.UserPersonaDetails;
import com.znsio.teswiz.tools.ReportPortalLogger;
import com.znsio.teswiz.tools.ScreenShotManager;
import io.cucumber.java.Scenario;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

import java.util.Map;
import java.util.Properties;

public class Hooks {
    private static final Logger LOGGER = Logger.getLogger(Hooks.class.getName());

    public void beforeScenario(Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        TestExecutionContext testExecutionContext = Runner.getTestExecutionContext(threadId);
        LOGGER.info(String.format("Hooks: ThreadId : %s In RunCukes - beforeScenario: %s", threadId,
                                  scenario.getName()));
        LOGGER.info(String.format("Hooks: Running test %s on %s", testExecutionContext.getTestName(),
                                  Runner.getPlatform().name()));
        if(!Runner.getPlatform().equals(Platform.api)) {
            testExecutionContext.addTestState(TEST_CONTEXT.SCREENSHOT_MANAGER, new ScreenShotManager());
        }
        testExecutionContext.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS,
                                          new UserPersonaDetails());
        SoftAssertions softly = new SoftAssertions();
        testExecutionContext.addTestState(TEST_CONTEXT.SOFT_ASSERTIONS, softly);
        addEnvironmentVariablesToReportPortal();
        addSystemPropertiesToReportPortal();
    }

    private void addEnvironmentVariablesToReportPortal() {
        Map<String, String> env = System.getenv();
        final String[] envVars = {""};
        env.forEach((k, v) -> envVars[0] += ("\t" + k + ":" + v + "\n"));
        ReportPortalLogger.logDebugMessage(
                String.format("Hooks: Environment Variables:%n%s", envVars[0]));
    }

    private void addSystemPropertiesToReportPortal() {
        Properties props = System.getProperties();
        final String[] propVars = {""};
        props.forEach((k, v) -> propVars[0] += ("\t" + k + ":" + v + "\n"));
        ReportPortalLogger.logDebugMessage(
                String.format("Hooks: System Properties:%n%s", propVars[0]));
    }

    public void afterScenario(Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        LOGGER.info(String.format("Hooks: ThreadId: %d In RunCukes - afterScenario: %s", threadId,
                                  scenario.getName()));
        Drivers.attachLogsAndCloseAllDrivers(scenario);
        SoftAssertions softly = Runner.getSoftAssertion(threadId);
        LOGGER.info("Hooks: Assert all soft assertions");
        softly.assertAll();
    }
}
