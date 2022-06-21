package com.znsio.sample.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.tools.Drivers;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.apache.log4j.Logger;

import java.util.Locale;

public class AppLaunchSteps {
    private static final Logger LOGGER = Logger.getLogger(AppLaunchSteps.class.getName());
    private final TestExecutionContext context;
    private final Drivers allDrivers;

    public AppLaunchSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread()
                                                               .getId());
        LOGGER.info("context: " + context.getTestName());
        allDrivers = (Drivers) context.getTestState(SAMPLE_TEST_CONTEXT.ALL_DRIVERS);
        LOGGER.info("allDrivers: " + (null == allDrivers));
    }

    @Given("{string} start {string}")
    public void startOn(String userPersona, String appName) {
        String[] appNameParts = appName.split("-");
        appName = appNameParts[0].toLowerCase(Locale.ROOT);
        String onPlatform = appNameParts[appNameParts.length - 1].toLowerCase(Locale.ROOT);
        LOGGER.info(System.out.printf("startOn - Persona:'%s', AppName: '%s', Platform: '%s'", userPersona, appName, onPlatform));
        context.addTestState(userPersona, userPersona);
        allDrivers.createDriverFor(userPersona, appName, Platform.valueOf(onPlatform), context);
    }

    @And("{string} starts {string} on {string}")
    public void startsOn(String userPersona, String appName, String browserName) {
        String[] appNameParts = appName.split("-");
        appName = appNameParts[0].toLowerCase(Locale.ROOT);
        String onPlatform = appNameParts[appNameParts.length - 1].toLowerCase(Locale.ROOT);
        LOGGER.info(System.out.printf("startOn - Persona:'%s', AppName: '%s', Browser: '%s', Platform: '%s'", userPersona, appName, browserName, onPlatform));
        context.addTestState(userPersona, userPersona);
        allDrivers.createDriverFor(userPersona, appName, browserName, Platform.valueOf(onPlatform), context);
    }
}
