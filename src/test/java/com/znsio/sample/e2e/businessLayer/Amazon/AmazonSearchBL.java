package com.znsio.sample.e2e.businessLayer.Amazon;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.Amazon.AmazonSearchScreen;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

public class AmazonSearchBL {
    private static final Logger LOGGER = Logger.getLogger(AmazonSearchBL.class.getName());
    private final TestExecutionContext context;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public AmazonSearchBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public AmazonSearchBL() {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.platform;
    }

    /**
     * Utility to verify whether the searched item is present in the search list
     * @param itemName item which is searched
     * @return {@link AmazonSearchBL}
     */
    public AmazonSearchBL isItemVisible(String itemName) {
        LOGGER.info("Verifying product list has item in it");
        boolean isVisible = AmazonSearchScreen.get().isVisibleItem(itemName);
        assertThat(isVisible).isTrue();
        return this;
    }

    /**
     * Navigating to Product detail page
     * @param itemName
     * @return {@link AmazonProductBL}
     */
    public AmazonProductBL navigateToProductDetail(String itemName) {
        LOGGER.info("Navigating to product detail page");
        AmazonSearchScreen.get().navigateToProductDetail(itemName);
        return new AmazonProductBL();
    }
}
