package com.znsio.teswiz.businessLayer.vodqa;

import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.screen.vodqa.VodqaScreen;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import static org.assertj.core.api.Assertions.assertThat;

public class VodqaBL {
    private static final Logger LOGGER = Logger.getLogger(VodqaBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public VodqaBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public VodqaBL() {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.getPlatform();
    }
    public VodqaBL login() {
        VodqaScreen.get().login();
        return this;
    }

    public VodqaBL scrollFromOneElementPointToAnother() {
        VodqaScreen.get().scrollFromOneElementPointToAnother();
        return this;
    }

    public VodqaBL isElementWithTextVisible() {
        boolean isScrollSuccessful= VodqaScreen.get().isElementWithTextVisible();
        assertThat(isScrollSuccessful).as("Scroll was not successful, text is not visible").isTrue();
        return this;
    }

    public VodqaBL selectScreenAndSwipeLeft(String screenName) {
        VodqaScreen.get().selectScreen(screenName).swipeLeft();
        return this;
    }

    public VodqaBL verifySwipe(String elementText) {
        assertThat(VodqaScreen.get().isSwipeSuccessful(elementText))
                .as("swipe was not successful")
                .isTrue();
        return this;
    }

    public VodqaBL selectScreenAndSwipeRight(String screenName) {
        VodqaScreen.get().selectScreen(screenName).swipeRight();
        return this;
    }

    public VodqaBL selectScreenAndSwipeByPassingPercentageAttributes(int atPercentScreenHeight, int fromPercentScreenWidth, int toPercentScreenWidth, String screenName) {
        VodqaScreen.get().selectScreen(screenName)
                .swipeByPassingPercentageAttributes(atPercentScreenHeight, fromPercentScreenWidth, toPercentScreenWidth);
        return this;
    }

    public VodqaBL scrollDownByScreenSizeOnVerticalSwipingScreen() {
        VodqaScreen.get().openVerticalSwipingScreen().scrollDownByScreenSize();
        return this;
    }
    public VodqaBL tapInTheMiddleOfTheScreen() {
        LOGGER.info("performTapActionInTheMiddle(): perform tap operation in the middle of the screen");
        VodqaScreen.get().tapInTheMiddle();
        return this;
    }

    public VodqaBL verifyUserMoveToNextPage(String pageHeading) {
        LOGGER.info("performTapActionInTheMiddle(): verify the operation has been executed successfully or not");
        assertThat(VodqaScreen.get().isPreviousPageHeadingVisible(pageHeading)).as(String.format("User is still on %s page", pageHeading)).isFalse();
        return this;
    }

    public VodqaBL appWorksInBackground(int time) {
        LOGGER.info("Validating app working in background");
        boolean isAppWorkInBackground =  VodqaScreen.get().putAppInBackground(time).validateAppWorkInBackground();
        assertThat(isAppWorkInBackground).as(String.format("App do not works in background")).isTrue();
        return this;
    }
}

