package com.znsio.sample.e2e.screen.android.ajio;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.ajio.CartScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CartScreenAndroid
        extends CartScreen {
    private static final String SCREEN_NAME = CartScreenAndroid.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final By byProductTitleId = By.id("com.ril.ajio:id/productTitle");
    private final Driver driver;
    private final Visual visually;


    public CartScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public String getActualProductName() {
        LOGGER.info("getCartProductName");
        visually.checkWindow(SCREEN_NAME, "Product in the cart");
        WebElement product = driver.waitTillElementIsPresent(byProductTitleId);
        product.click();
        String productTitle = product.getText();
        LOGGER.info("productTitle in the cart" + productTitle);
        return productTitle;
    }
}
