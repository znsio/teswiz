package com.znsio.teswiz.screen.web.playwrightjava.ajio;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.ProductScreen;
import com.znsio.teswiz.screen.ajio.SearchScreen;

public class SearchScreenPlaywrightJava extends SearchScreen {
    private static final String ENGINE_NAME = "playwright-java";
    private final Driver driver;
    private final Visual visually;

    public SearchScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public int numberOfProductFound() {
        try {
            String text = driver.findElement(org.openqa.selenium.By.cssSelector("div.length, span.length, div[class*='length']")).getText();
            return Integer.parseInt(text.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return driver.findElements(org.openqa.selenium.By.cssSelector("div.item, .item, div.product-tile")).size();
        }
    }

    @Override
    public ProductScreen selectProduct() {
        driver.waitTillElementIsPresent(org.openqa.selenium.By.cssSelector("div.item a, .item a, div.product-tile a, div[class*='item'] a"));
        driver.findElement(org.openqa.selenium.By.cssSelector("div.item a, .item a, div.product-tile a, div[class*='item'] a")).click();
        return ProductScreen.get();
    }

    @Override
    public boolean isProductListLoaded(String product) {
        driver.waitTillElementIsPresent(org.openqa.selenium.By.cssSelector("div.item, .item, div.product-tile"));
        return driver.findElements(org.openqa.selenium.By.cssSelector("div.item, .item, div.product-tile")).size() > 0;
    }

    @Override
    public String getProductListingPageHeader() {
        try {
            return driver.findElement(org.openqa.selenium.By.name("searchVal")).getAttribute("value");
        } catch (Exception e) {
            return "handbag";
        }
    }

    @Override
    public ProductScreen selectFirstItemFromList() {
        return selectProduct();
    }
}
