package com.znsio.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.businessLayer.AppBL;
import com.znsio.e2e.businessLayer.EchoBL;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Drivers;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

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

    @Then("I try to login again with invalid credentials - {string}, {string}")
    public void iTryToLoginAgainWithInvalidCredentials(String username, String password) {
        LOGGER.info(System.out.printf("iTryToLoginAgainWithInvalidCredentials - Username: '%s', Password:'%s'", username, password));
        new AppBL().loginAgain(username, password);
    }

    @When("{string} login with invalid credentials - {string}, {string}")
    public void loginWithInvalidCredentials(String userPersona, String username, String password) {
        Platform onPlatform = allDrivers.getPlatformForUser(userPersona);
        LOGGER.info(System.out.printf("LoginWithInvalidCredentials - Persona:'%s', Username: '%s', Password:'%s', Platform: '%s'", SAMPLE_TEST_CONTEXT.ME, username, password, onPlatform.name()));
        new AppBL(userPersona, onPlatform).provideInvalidDetailsForSignup(username, password);
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
}
