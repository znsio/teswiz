package com.znsio.sample.e2e.businessLayer.Amazon;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.Amazon.LoginScreen;
import org.apache.log4j.Logger;


public class LoginBL {
    private static final Logger LOGGER = Logger.getLogger(LoginBL.class.getName());
    private final TestExecutionContext context;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public LoginBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public LoginBL() {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.platform;
    }

    /**
     * Utility to login to amazon account
     * @return {@link HomeBL}
     */
    public HomeBL loginToAmazon() {
        LOGGER.info("Logging to Amazon with Credentials");
        LoginScreen.get()
                .loginWithCredentials(Runner.getTestData("userName"), Runner.getTestData("password"));
        LOGGER.info("Login is done");
        return new HomeBL();
    }
}