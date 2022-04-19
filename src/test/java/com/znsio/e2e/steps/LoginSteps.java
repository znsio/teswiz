package com.znsio.e2e.steps;

import com.context.*;
import com.znsio.e2e.businessLayer.*;
import com.znsio.e2e.entities.*;
import com.znsio.e2e.runner.*;
import com.znsio.e2e.tools.*;
import io.cucumber.java.en.*;
import org.apache.log4j.*;

import java.util.*;

public class LoginSteps {
    private static final Logger LOGGER = Logger.getLogger(LoginSteps.class.getName());
    private final TestExecutionContext context;
    private final Drivers allDrivers;

    public LoginSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
        allDrivers = (Drivers) context.getTestState(SAMPLE_TEST_CONTEXT.ALL_DRIVERS);
        LOGGER.info("allDrivers: " + (null == allDrivers));
    }

    @When("I login with invalid credentials - {string}, {string}")
    public void iLoginWithInvalidCredentials(String username, String password) {
        LOGGER.info(System.out.printf("iLoginWithInvalidCredentials - Persona:'%s', Username: '%s', Password:'%s', Platform: '%s'", SAMPLE_TEST_CONTEXT.ME, username, password, Runner.platform));
        allDrivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.platform, context);
        context.addTestState(SAMPLE_TEST_CONTEXT.ME, username);
        new AppBL(SAMPLE_TEST_CONTEXT.ME, Runner.platform).provideInvalidDetailsForSignup(username, password);
    }

    @When("I go back")
    public void iGoBack() {
        new AppBL().goBack();
    }

    @Given("{string} login with invalid credentials - {string}, {string} on {string}")
    public void loginWithInvalidCredentialsOn(String userPersona, String username, String password, String onPlatform) {
        LOGGER.info(System.out.printf("LoginWithInvalidCredentials - Persona:'%s', Username: '%s', Password:'%s', Platform: '%s'", userPersona, username, password, onPlatform));
        context.addTestState(userPersona, username);
        allDrivers.createDriverFor(userPersona, Platform.valueOf(onPlatform), context);
        new AppBL(userPersona, Platform.valueOf(onPlatform)).provideInvalidDetailsForSignup(username, password);
    }

    @When("{string} login again with invalid credentials - {string}, {string}")
    public void loginAgainWithInvalidCredentials(String userPersona, String username, String password) {
        Platform onPlatform = allDrivers.getPlatformForUser(userPersona);
        LOGGER.info(System.out.printf("LoginWithInvalidCredentials - Persona:'%s', Username: '%s', Password:'%s', Platform: '%s'", SAMPLE_TEST_CONTEXT.ME, username, password, onPlatform.name()));
        new AppBL(userPersona, onPlatform).loginAgain(username, password);
    }

    @Then("I can echo {string} in the message box")
    public void iCanEchoInTheMessageBox(String message) {
        new EchoBL().echoMessage(message);
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
}
