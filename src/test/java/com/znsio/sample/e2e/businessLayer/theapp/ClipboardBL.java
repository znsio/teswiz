package com.znsio.sample.e2e.businessLayer.theapp;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.theapp.AppLaunchScreen;
import com.znsio.sample.e2e.screen.theapp.ClipboardDemoScreen;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

public class ClipboardBL {
    private static final Logger LOGGER = Logger.getLogger(ClipboardBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public ClipboardBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread()
                              .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public ClipboardBL() {
        long threadId = Thread.currentThread()
                              .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.platform;
    }

    public ClipboardBL saveContentInClipboard(String content) {
        return setContentInClipboard(content).verifyContentIsSaved(content);
    }

    public ClipboardBL verifyContentIsSaved(String contentExpectedInClipboard) {
        boolean isAddedContentExistingInClipboard = ClipboardDemoScreen.get()
                                                                       .doesAddedContentExistInClipboard();
        assertThat(isAddedContentExistingInClipboard).as(String.format("Content: '%s' is not added in the clipboard", contentExpectedInClipboard))
                                                     .isTrue();
        return this;
    }

    public ClipboardBL setContentInClipboard(String content) {
        context.addTestState("contentInClipboard", content);
        AppLaunchScreen.get()
                       .goToClipboardDemo()
                       .setInClipboard(content);
        return this;
    }
}
