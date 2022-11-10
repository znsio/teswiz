package com.znsio.sample.e2e.businessLayer.Amazon;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.Amazon.CartScreen;
import org.apache.log4j.Logger;
import static org.assertj.core.api.Assertions.assertThat;


public class CartBL {
    private static final Logger LOGGER = Logger.getLogger(CartBL.class.getName());
    private final TestExecutionContext context;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public CartBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public CartBL() {
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
    public CartBL isProductExistInCart(String itemName) {
        LOGGER.info("isProductExistInCart - Verifying product is present in cart");
        boolean isPresent = CartScreen.get().verifyItemInCart(itemName);
        assertThat(isPresent).isTrue();
        return this;
    }
}
