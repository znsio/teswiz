package com.znsio.sample.e2e.businessLayer.Amazon;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.Amazon.HomeScreen;
import org.apache.log4j.Logger;


public class HomeBL {
    private static final Logger LOGGER = Logger.getLogger(HomeBL.class.getName());
    private final TestExecutionContext context;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public HomeBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public HomeBL() {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.platform;
    }

    /**
     * Utility to search for product, check is the itemVisible and navigate to PD
     * @param itemName parameter of searched product
     * @return {@link ProductBL}
     */
    public ProductBL searchAndNavigateToProductDetail(String itemName) {
        HomeScreen.get().searchItem(itemName);
        return new SearchBL()
                .isItemVisible(itemName)
                .navigateToProductDetail(itemName);
    }
}
