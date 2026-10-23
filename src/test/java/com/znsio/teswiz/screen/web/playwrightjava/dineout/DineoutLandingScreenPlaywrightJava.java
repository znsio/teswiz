package com.znsio.teswiz.screen.web.playwrightjava.dineout;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.dineout.DineoutLandingScreen;
import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class DineoutLandingScreenPlaywrightJava extends DineoutLandingScreen {
    private static final String SCREEN_NAME = DineoutLandingScreenPlaywrightJava.class.getSimpleName();
    private static final String NOT_YET_IMPLEMENTED = " not yet implemented";

    private static final By DEFAULT_CITY = By.xpath("//a[@aria-label='Mumbai']");
    private static final By RESTAURANT_SEARCH = By.id("restaurantSearch");
    private static final By LOCATION_FILTER = By.xpath("//li[text()='Location']");
    private static final By SUBMIT_CITY = By.xpath("//section//button[@value='Submit']");

    private final Driver driver;
    private final Visual visually;

    public DineoutLandingScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        visually.checkWindow(SCREEN_NAME, "Launch screen");
    }

    @Override
    public DineoutLandingScreen selectDefaultCity() {
        driver.waitTillElementIsPresent(DEFAULT_CITY).click();
        return this;
    }

    @Override
    public DineoutLandingScreen selectCity(String city) {
        visually.checkWindow(SCREEN_NAME, "On home page");
        WebElement restaurantSearch = driver.waitTillElementIsPresent(RESTAURANT_SEARCH);
        restaurantSearch.clear();
        restaurantSearch.sendKeys(city);
        visually.checkWindow(SCREEN_NAME, "Selected default city");
        driver.waitTillElementIsPresent(LOCATION_FILTER).click();
        driver.waitTillElementIsPresent(SUBMIT_CITY).click();
        visually.checkWindow(SCREEN_NAME, "Selected first area in city - " + city);
        return this;
    }

    @Override
    public DineoutLandingScreen searchCuisine(String cuisine) {
        throw new NotImplementedException(
                SCREEN_NAME + ":" + new Throwable().getStackTrace()[0].getMethodName() + NOT_YET_IMPLEMENTED);
    }
}
