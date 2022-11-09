package com.znsio.sample.e2e.businessLayer.Amazon;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.Amazon.AmazonLoginScreen;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;


public class AmazonLoginBL {
    private static final Logger LOGGER = Logger.getLogger(AmazonLoginBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;
    public String userName;
    public String password;

    public AmazonLoginBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public AmazonLoginBL() {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.platform;
    }

    /**
     * Utility to login to amazon account
     * @return {@link AmazonHomeBL}
     */
    public AmazonHomeBL loginToAmazon() {
        LOGGER.info("Logging to Amazon with Credentials");
        try {
            retrieveJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
        AmazonLoginScreen.get().loginWithCredentials(userName, password);
        LOGGER.info("Login is done");
        return new AmazonHomeBL();
    }

    /**
     * Utiltiy to parse json file and extract username and password
     * @throws Exception
     */
    private void retrieveJson() throws Exception {
        JSONObject json = (JSONObject) new JSONParser()
                .parse(new FileReader("./src/test/resources/testData.json"));
        userName = (String) json.get("userName");
        password = (String) json.get("password");
    }
}