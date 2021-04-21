package com.znsio.e2e.businessLayer;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.screen.HomeScreen;
import com.znsio.e2e.screen.LoginScreen;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AppBL {
    private static final Logger LOGGER = Logger.getLogger(AppBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public AppBL (String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public LoginBL provideValidDetailsForSignup (String username, String password) {
        String expectedErrorMessage = "Invalid login credentials, please try again";
        LoginScreen loginScreen = HomeScreen.get()
                .selectLoginTest()
                .enterLoginDetails(username, password)
                .login();
        String actualErrorMessage = loginScreen
                .getInvalidLoginError();
        LOGGER.info("actualErrorMessage: " + actualErrorMessage);

        loginScreen.dismissAlert();

        assertThat(actualErrorMessage)
                .as("Invalid login credentials error message is incorrect")
                .isEqualTo(expectedErrorMessage);
        return new LoginBL(currentUserPersona, currentPlatform);
    }
}
