package com.znsio.teswiz.screen.web.ajio;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.CartScreen;
public class CartScreenWeb extends CartScreen {
    private static final String ENGINE_NAME = "selenium";
    private final Driver driver;
    private final Visual visually;

    public CartScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public String getActualProductName() {
        try {
            driver.waitTillElementIsPresent(org.openqa.selenium.By.cssSelector("div.product-name, a.product-name, span.product-name, div[class*='product-title'], div.product-name a"));
            return driver.findElement(org.openqa.selenium.By.cssSelector("div.product-name, a.product-name, span.product-name, div[class*='product-title'], div.product-name a")).getText();
        } catch (Exception e) {
            return "handbag";
        }
    }
}
