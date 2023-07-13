package com.znsio.teswiz.screen.ios.ajio;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.HomeScreen;
import com.znsio.teswiz.screen.ajio.SearchScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

import java.util.Map;

public class HomeScreenIOS
        extends HomeScreen {
    private static final String SCREEN_NAME = HomeScreenIOS.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final By byStartSearchBoxId = By.id("Home_Search_Label");
    private static final By byUploadPhotoButtonId = By.id("Upload a Photo");
    private static final By byImageId = By.id("Photo, Yellow Handbag, July 06, 6:04 PM");
    private final Driver driver;
    private final Visual visually;

    public HomeScreenIOS(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public SearchScreen searchByImage() {
        driver.waitTillElementIsPresent(byImageId).click();
        LOGGER.info("Clicked on Image");
        return SearchScreen.get();
    }

    @Override
    public HomeScreen attachFileToDevice(Map imageData) {
        driver.waitTillElementIsPresent(byStartSearchBoxId).click();
        LOGGER.info("Clicked on HomePage Search Box");
        visually.checkWindow(SCREEN_NAME, "Get Search Screen");
        driver.waitTillElementIsPresent(byUploadPhotoButtonId).click();
        LOGGER.info("Clicked on Upload Photo Button");
        return this;
    }

}
