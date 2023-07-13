package com.znsio.teswiz.screen.ios.ajio;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.CartScreen;
import com.znsio.teswiz.screen.ajio.ProductScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.znsio.teswiz.tools.Wait.waitFor;

public class ProductScreenIOS
        extends ProductScreen {
    private static final String SCREEN_NAME = ProductScreenIOS.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final By byProductNameId = By.id("V2Pdp_brand_ProductName");
    private static final By byAddToCartButtonId = By.id("Add To Bag");
    private static final By byViewBagButtonId = By.id("V2PDP_BottomFloatingView_ATC");
    private static final By bySelectSizeMediumXpath = By.xpath("//XCUIElementTypeCollectionView//XCUIElementTypeStaticText[@label='M']");
    private static final By byAddToBagId = By.id("V2PDP_bottomView_AddToButtonView");
    private final Driver driver;
    private final Visual visually;

    public ProductScreenIOS(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public CartScreen addProductToCart() {
        LOGGER.info("addProductToCart");
        driver.waitTillElementIsPresent(byAddToCartButtonId).click();
        visually.checkWindow(SCREEN_NAME, "Add to cart");
        driver.waitTillElementIsPresent(bySelectSizeMediumXpath).click();
        driver.waitTillElementIsPresent(byAddToBagId).click();
        driver.waitTillElementIsPresent(byViewBagButtonId).click();
        return CartScreen.get();
    }

    @Override
    public String getProductName() {
        LOGGER.info("getProductName");
        waitFor(5);
        WebElement product = driver.waitTillElementIsPresent(byProductNameId);
        String productName = product.getText();
        visually.checkWindow(SCREEN_NAME, "Product Details");
        LOGGER.info("Product Name: " + productName);
        return productName;
    }

    @Override
    public boolean isProductDetailsLoaded() {
        return false;
    }

    @Override
    public ProductScreen flickImage() {
        return null;
    }

    @Override
    public String isElementIdChanged() {
        return null;
    }

}
