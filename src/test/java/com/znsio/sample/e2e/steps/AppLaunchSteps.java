package com.znsio.sample.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.TEST_CONTEXT;
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
        String evaluatedBrowser = this.evaluateBrowserType(browserName);
        LOGGER.info(System.out.printf("startOn - Persona:'%s', AppName: '%s', Browser: '%s', Platform: '%s'", userPersona, appName, evaluatedBrowser, onPlatform));
        context.addTestState(userPersona, userPersona);
        allDrivers.createDriverFor(userPersona, appName, evaluatedBrowser, Platform.valueOf(onPlatform), context);
    }

    /**
     * method to evaluate whether the scenario is to be executed in normal web browser view or mobile emulation specific view
     * if mobile emulation is detected, it adds the targeted device name in context that will be utilized while creating WebDriver
     * @param browserName (like 'firefox-mobile2', 'chrome-tab1', etc)
     * @return actual browser name (like 'firefox', 'chrome', etc)
     */
    private String evaluateBrowserType(String browserName) {
        String[] details = browserName.split("-");
        if (details.length == 1) {
            //when user passes values like 'chrome', 'firefox', etc
            return browserName;
        }

        //when user passes values like 'chrome-mobile1', 'safari-tab2', etc
        String browser = details[0];
        String mobileEmulation = details[1];

        //TODO add logic to get deviceName from Browser Config json file based on 'mobileEmulation' variable
        // & add it in context for future reference

        //upon fetching the value of deviceName, replace it in below line
        context.addTestState(TEST_CONTEXT.MOBILE_EMULATION_DEVICE, "deviceName");

        return browser;
    }
}
