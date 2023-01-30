package com.znsio.sample.e2e.screen.web.dineout;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.dineout.DineoutLandingScreen;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class DineoutLandingScreenWeb
        extends DineoutLandingScreen {

    private final Driver driver;
    private final Visual visually;
    private static final String SCREEN_NAME = DineoutLandingScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final String NOT_YET_IMPLEMENTED = " not yet implemented";

    public DineoutLandingScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        visually.checkWindow(SCREEN_NAME, "Launch screen");
    }

    @Override
    public DineoutLandingScreen selectDefaultCity() {
        driver.waitTillElementIsPresent(By.xpath("//a[@aria-label='Mumbai']"))
              .click();
        return this;
    }

    @Override
    public DineoutLandingScreen selectCity(String city) {
        visually.checkWindow(SCREEN_NAME, "On home page");
        WebElement restaurantSearch = driver.waitTillElementIsPresent(By.id("restaurantSearch"));
        restaurantSearch.clear();
        restaurantSearch.sendKeys(city);
        visually.checkWindow(SCREEN_NAME, "Selected default city");
        driver.waitTillElementIsPresent(By.xpath("//li[text()='Location']"))
              .click();
        driver.waitTillElementIsPresent(By.xpath("//section//button[@value='Submit']"))
              .click();
        visually.checkWindow(SCREEN_NAME, "Selected first area in city - " + city);
        return this;
    }

    @Override
    public DineoutLandingScreen searchCuisine(String cusine) {
        throw new NotImplementedException(SCREEN_NAME + ":" + new Throwable().getStackTrace()[0].getMethodName() + NOT_YET_IMPLEMENTED);
    }
}
