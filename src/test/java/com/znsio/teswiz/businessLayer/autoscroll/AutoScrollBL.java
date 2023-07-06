package com.znsio.teswiz.businessLayer.autoscroll;

import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.screen.autoscroll.AutoScrollScreen;
import org.apache.log4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;


public class AutoScrollBL {

    private static final Logger LOGGER = Logger.getLogger(AutoScrollBL.class.getName());
    private final TestExecutionContext context;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public AutoScrollBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public AutoScrollBL goToDropdownWindow() {
        AutoScrollScreen.get().goToDropdownWindow();
        return this;
    }

    public AutoScrollBL verifyScrollInDynamicLayerFunctionality() {
        LOGGER.info("verifying scroll in dynamic layer functionality");
        assertThat(AutoScrollScreen.get().scrollInDynamicLayer().isScrollSuccessful())
                .as("scroll did not happen in inner dropdown")
                .isTrue();
        return this;
    }
}