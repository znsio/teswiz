package com.znsio.teswiz.businessLayer.googlesearch;

import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.screen.googlesearch.GoogleSearchLandingScreen;
import org.assertj.core.api.SoftAssertions;

public class GoogleSearchBL {
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public GoogleSearchBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public SearchResultsBL searchFor(String searchText) {
        GoogleSearchLandingScreen.get().searchFor(searchText);
        return new SearchResultsBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform());
    }
}