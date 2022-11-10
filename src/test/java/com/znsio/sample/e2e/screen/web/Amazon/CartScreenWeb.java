package com.znsio.sample.e2e.screen.web.Amazon;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.Amazon.CartScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

public class CartScreenWeb extends CartScreen {
    private final Driver driver;
    private final Visual visually;
    private static final String SCREEN_NAME = CartScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);

    private static final By cartHeading = By.cssSelector("div[class='a-row'] h1");
    private static final By productName = By.xpath("//span[@class='a-truncate-cut']");

    public CartScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    /**
     * Utility verifying product present inside cart
     * @param itemName
     * @return
     */
    @Override
    public boolean verifyItemInCart(String itemName) {
        LOGGER.info("Verification by cart heading and product name present in cart");
        return driver.isElementPresent(cartHeading)
                && driver.findElement(productName).getText().contains(itemName);
    }
}
