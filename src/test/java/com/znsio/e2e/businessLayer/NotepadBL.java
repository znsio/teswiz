package com.znsio.e2e.businessLayer;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.screen.NotepadScreen;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

public class NotepadBL {
    private static final Logger LOGGER = Logger.getLogger(NotepadBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public NotepadBL() {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.platform;
        LOGGER.info("NotepadBL created");
    }

    public NotepadBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
        LOGGER.info("NotepadBL created for userPersona: " + userPersona);
    }

    public NotepadBL verifyLaunched() {
        NotepadScreen.get().takeScreenshot();
        return this;
    }

    public NotepadBL typeMessage(String message) {
        NotepadScreen.get().typeMessage(message);
        return this;
    }
}
