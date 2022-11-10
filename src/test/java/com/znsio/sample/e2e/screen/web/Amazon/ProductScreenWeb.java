package com.znsio.sample.e2e.screen.web.Amazon;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.Amazon.CartScreen;
import com.znsio.sample.e2e.screen.Amazon.ProductScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

public class ProductScreenWeb extends ProductScreen {
    private final Driver driver;
    private final Visual visually;
    private static final String SCREEN_NAME = ProductScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);

    private final By addToCart = By.id("add-to-cart-button");
    private final By moveToCart = By.id("attach-sidesheet-view-cart-button");

    public ProductScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    /**
     * Utility adding the product to cart and navigating to cart page
     * @param itemName
     * @return
     */
    @Override
    public CartScreen addAndNavigateToCart(String itemName) {
        LOGGER.info("Adding product to the cart");
        driver.findElement(addToCart).click();
        LOGGER.info("Navigating to Cart");
        driver.waitForClickabilityOf(moveToCart).click();
        return CartScreen.get();
    }
}
