package com.znsio.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.businessLayer.AppBL;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Drivers;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

public class LoginSteps {
    private static final Logger LOGGER = Logger.getLogger(LoginSteps.class.getName());
    private final TestExecutionContext context;
    private final Drivers allDrivers;
    private String userPersona;

    public LoginSteps () {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
        allDrivers = (Drivers) context.getTestState(SAMPLE_TEST_CONTEXT.ALL_DRIVERS);
        LOGGER.info("allDrivers: " + (null == allDrivers));
    }

    @When("I login with invalid credentials - {string}, {string}")
    public void iLoginWithInvalidCredentials (String username, String password) {
        LOGGER.info("iLoginWithInvalidCredentials - " + Runner.platform);
        allDrivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.platform, context);
        context.addTestState(SAMPLE_TEST_CONTEXT.ME, username);
        new AppBL(SAMPLE_TEST_CONTEXT.ME, Runner.platform).provideValidDetailsForSignup(username, password);
    }

    @Given("{string} login with invalid credentials - {string}, {string} on {string}")
    public void loginWithInvalidCredentialsOn (String userPersona, String username, String password, String onPlatform) {
        context.addTestState(userPersona, username);
        allDrivers.createDriverFor(userPersona, Platform.valueOf(onPlatform), context);
        new AppBL(userPersona, Platform.valueOf(onPlatform)).provideValidDetailsForSignup(username, password);
    }
}
