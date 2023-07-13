package com.znsio.teswiz.screen.ios.ajio;

import com.applitools.eyes.appium.Target;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.CartScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.znsio.teswiz.tools.Wait.waitFor;

public class CartScreenIOS
        extends CartScreen {
    private static final String SCREEN_NAME = CartScreenIOS.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final By byProductTitleId = By.id("cart_cartController_cartCell_productNameLabel");
    private static final By byWishlistPopUp = By.id("OK");
    private final Driver driver;
    private final Visual visually;

    public CartScreenIOS(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public String getActualProductName() {
        LOGGER.info("getCartProductName");
        waitFor(5);
        if (driver.isElementPresent(byWishlistPopUp)) {
            visually.check(SCREEN_NAME, "Get Actual Product Name",
                    Target.window().fully().layout(byWishlistPopUp));
            driver.waitTillElementIsPresent(byWishlistPopUp).click();
        }
        WebElement product = driver.waitTillElementIsPresent(byProductTitleId);
        visually.check(SCREEN_NAME, "Get Actual Product Name",
                Target.window().fully().layout(product));
        product.click();
        String productTitle = product.getText();
        LOGGER.info("productTitle in the cart" + productTitle);
        return productTitle;
    }
}
