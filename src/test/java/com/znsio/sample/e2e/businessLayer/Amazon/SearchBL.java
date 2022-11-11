package com.znsio.sample.e2e.businessLayer.Amazon;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.Amazon.SearchScreen;
import org.apache.log4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchBL {
    private static final Logger LOGGER = Logger.getLogger(SearchBL.class.getName());
    private final TestExecutionContext context;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public SearchBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public SearchBL() {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.platform;
    }

    /**
     * Utility to verify whether the searched item is present in the search list
     * @param itemName item which is searched
     * @return {@link SearchBL}
     */
    public SearchBL isItemVisible(String itemName) {
        LOGGER.info(String.format("isItemVisible-Platform %s : Verifying item is visible", Runner.platform));
        boolean isVisible = SearchScreen.get().isVisibleItem(itemName);
        assertThat(isVisible).isTrue();
        return this;
    }

    /**
     * Navigating to Product detail page
     * @param itemName
     * @return {@link ProductBL}
     */
    public ProductBL navigateToProductDetail(String itemName) {
        LOGGER.info(String.format("navigateToProductDetail-Platform %s : Navigating to product detail", Runner.platform));
        LOGGER.info("Navigating to product detail page");
        SearchScreen.get().navigateToProductDetail(itemName);
        return new ProductBL();
    }
}
