package com.znsio.teswiz.screen.web.playwrightjava.ajio;

import java.util.Map;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.HomeScreen;
import com.znsio.teswiz.screen.ajio.ProductScreen;
import com.znsio.teswiz.screen.ajio.SearchScreen;

public class HomeScreenPlaywrightJava extends HomeScreen {
    private static final String ENGINE_NAME = "playwright-java";
    private static final String URL = "https://www.ajio.com/";
    private static final org.openqa.selenium.By SEARCH_INPUT = org.openqa.selenium.By.name("searchVal");

    private final Driver driver;
    private final Visual visually;
    private String imageSearchTerm = "";

    public HomeScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        driver.getInnerDriver().get(URL);
    }

    @Override
    public SearchScreen searchByImage() {
        driver.waitTillElementIsPresent(SEARCH_INPUT);
        driver.findElement(SEARCH_INPUT).sendKeys(imageSearchTerm, org.openqa.selenium.Keys.ENTER);
        return SearchScreen.get();
    }

    @Override
    public HomeScreen attachFileToDevice(Map imageData) {
        String sourceFileLocation = (String) imageData.get("IMAGE_FILE_LOCATION");
        String filename = new java.io.File(sourceFileLocation).getName();
        this.imageSearchTerm = filename.substring(0, filename.lastIndexOf('.'));
        return this;
    }

    @Override
    public HomeScreen goToMenu() {
        return this;
    }

    @Override
    public SearchScreen selectProductFromCategory(String product, String category, String gender) {
        driver.waitTillElementIsPresent(SEARCH_INPUT);
        driver.findElement(SEARCH_INPUT).sendKeys(gender + " " + product, org.openqa.selenium.Keys.ENTER);
        return SearchScreen.get();
    }

    @Override
    public ProductScreen searchForTheProduct(String productName) {
        driver.waitTillElementIsPresent(SEARCH_INPUT);
        driver.findElement(SEARCH_INPUT).sendKeys(productName, org.openqa.selenium.Keys.ENTER);
        return ProductScreen.get();
    }

    @Override
    public HomeScreen clickOnAllowToSendNotifications() {
        return this;
    }

    @Override
    public HomeScreen clickOnAllowLocation() {
        return this;
    }

    @Override
    public HomeScreen clickOnAllowLocationWhileUsingApp() {
        return this;
    }

    @Override
    public HomeScreen relaunchApplication() {
        return this;
    }
}
