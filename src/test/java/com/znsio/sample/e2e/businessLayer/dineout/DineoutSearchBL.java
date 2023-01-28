package com.znsio.sample.e2e.businessLayer.dineout;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.dineout.DineoutLandingScreen;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

public class DineoutSearchBL {
    private static final Logger LOGGER = Logger.getLogger(DineoutSearchBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public DineoutSearchBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread()
                              .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public DineoutSearchBL() {
        long threadId = Thread.currentThread()
                              .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.platform;
    }

    public DineoutSearchBL selectCity(String city) {
        DineoutLandingScreen.get()
                            .selectDefaultCity()
                            .selectCity(city);
        return this;
    }

    public DineoutSearchBL searchForCusine(String cusine) {
        DineoutLandingScreen.get()
                            .searchCuisine(cusine);
        return this;
    }
}
