package com.znsio.teswiz.screen.android.ajio;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.CartScreen;
import com.znsio.teswiz.screen.ajio.ProductScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ProductScreenAndroid
        extends ProductScreen {
    private static final String SCREEN_NAME = ProductScreenAndroid.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final By byProductNameId = By.id("com.ril.ajio:id/product_name");
    private static final By byAddToCartButtonId = By.id("com.ril.ajio:id/add_to_cart_tv");
    private static final By byViewBagButtonXpath = By.xpath(
            "//android.widget.TextView[@text='View Bag']");
    private static final By byBrandNameId = By.id("com.ril.ajio:id/product_name");
    private final Driver driver;
    private final Visual visually;

    public ProductScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public CartScreen addProductToCart() {
        LOGGER.info("addProductToCart");
        driver.waitTillElementIsPresent(byAddToCartButtonId).click();

        visually.checkWindow(SCREEN_NAME, "Add to cart");
        driver.waitTillElementIsPresent(byViewBagButtonXpath).click();
        return CartScreen.get();
    }

    @Override
    public String getProductName() {
        LOGGER.info("getProductName");
        driver.waitTillElementIsPresent(byBrandNameId).click();
        WebElement product = driver.waitTillElementIsPresent(byProductNameId);
        product.click();
        String productName = product.getText();
        visually.checkWindow(SCREEN_NAME, "Product Details");
        LOGGER.info("Product Name: " + productName);
        return productName;
    }

    @Override
    public boolean isProductDetailsLoaded() {
        LOGGER.info("Verifying if Product Details page is loaded");
        return false;
    }

    @Override
    public ProductScreen swipeAndViewImage() {
        LOGGER.info("Performing swipe to view the product image");
        return this;
    }

    @Override
    public boolean isSecondImageVisible() {
        LOGGER.info("Verifying if second image of product is visible ");
        return false;
    }

}
