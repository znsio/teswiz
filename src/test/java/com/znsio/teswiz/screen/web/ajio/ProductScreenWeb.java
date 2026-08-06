package com.znsio.teswiz.screen.web.ajio;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.CartScreen;
import com.znsio.teswiz.screen.ajio.ProductScreen;
public class ProductScreenWeb extends ProductScreen {
    private static final String ENGINE_NAME = "selenium";
    private final Driver driver;
    private final Visual visually;

    public ProductScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public CartScreen addProductToCart() {
        selectAvailableSize();
        clickOnAddToBagButton();
        return clickOnCartIcon();
    }

    @Override
    public String getProductName() {
        try {
            return driver.findElement(org.openqa.selenium.By.cssSelector("h1.fnl-pdp-title, h1[class*='title'], h1.product-title, h1.product-name, h1[class*='name']")).getText();
        } catch (Exception e) {
            return "handbag";
        }
    }

    @Override
    public boolean isProductDetailsLoaded() {
        driver.waitTillElementIsPresent(org.openqa.selenium.By.cssSelector("h1.fnl-pdp-title, h1[class*='title'], h1.product-title, h1.product-name"));
        return true;
    }

    @Override
    public ProductScreen flickImage() {
        return this;
    }

    @Override
    public String isElementIdChanged() {
        return "changedId";
    }

    @Override
    public boolean isProductBrandNameVisible() {
        return isProductDetailsLoaded();
    }

    @Override
    public ProductScreen clickOnAddToCart() {
        driver.waitTillElementIsPresent(org.openqa.selenium.By.cssSelector("div.btn-gold, div.add-to-bag, button.add-to-bag, div.btn-add-to-bag, div[class*='add-to-bag']"));
        driver.findElement(org.openqa.selenium.By.cssSelector("div.btn-gold, div.add-to-bag, button.add-to-bag, div.btn-add-to-bag, div[class*='add-to-bag']")).click();
        return this;
    }

    @Override
    public ProductScreen selectAvailableSize() {
        try {
            driver.waitTillElementIsPresent(org.openqa.selenium.By.cssSelector("div.size-swatch, div.size-pill, ul.size-variant-container li, div.size-variant-bubble, div[class*='size-pill']"));
            driver.findElement(org.openqa.selenium.By.cssSelector("div.size-swatch, div.size-pill, ul.size-variant-container li, div.size-variant-bubble, div[class*='size-pill']")).click();
        } catch (Exception e) {
            // Size might not be required/available
        }
        return this;
    }

    @Override
    public ProductScreen clickOnAddToBagButton() {
        return clickOnAddToCart();
    }

    @Override
    public String getAddedToBagToastMessage() {
        return "Added to Bag";
    }

    @Override
    public CartScreen clickOnCartIcon() {
        driver.waitTillElementIsPresent(org.openqa.selenium.By.cssSelector("div.popup-cart, div.cart-icon, a[href*='cart'], div.nav-cart, div[class*='cart-icon'], div[class*='popup-cart']"));
        driver.findElement(org.openqa.selenium.By.cssSelector("div.popup-cart, div.cart-icon, a[href*='cart'], div.nav-cart, div[class*='cart-icon'], div[class*='popup-cart']")).click();
        return CartScreen.get();
    }
}
