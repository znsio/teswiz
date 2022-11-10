package com.znsio.sample.e2e.businessLayer.Amazon;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.Amazon.AmazonCartScreen;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import org.junit.Assert;

import static org.hamcrest.CoreMatchers.is;

public class AmazonCartBL {
    private static final Logger LOGGER = Logger.getLogger(AmazonCartBL.class.getName());
    private final TestExecutionContext context;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public AmazonCartBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public AmazonCartBL() {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.platform;
    }

    /**
     * Utility to verify the Cart is holding the searched item in it
     * @param itemName searched product
     */
    public AmazonCartBL verifyItemInCart(String itemName) {
        LOGGER.info("Verifying product is present in cart");
        boolean isPresent = AmazonCartScreen.get().verifyItemInCart(itemName);
        Assert.assertThat("Item Not present in Cart", isPresent, is(true));
        return this;
    }
}
