package com.znsio.sample.e2e.screen.web.Amazon;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.Amazon.AmazonHomeScreen;
import com.znsio.sample.e2e.screen.Amazon.AmazonSearchScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

public class AmazonHomeScreenWeb extends AmazonHomeScreen {

    private final Driver driver;
    private final Visual visually;
    private static final String SCREEN_NAME = AmazonHomeScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);

    public static final By searchBox = By.id("twotabsearchtextbox");
    public static final By searchIcon = By.id("nav-search-submit-button");

    public AmazonHomeScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    /**
     * Utility for searching the item in searchbox
     * @param itemName
     * @return
     */
    @Override
    public AmazonSearchScreen searchItem(String itemName) {
        LOGGER.info("Searching for product");
        driver.waitForClickabilityOf(searchBox).click();
        driver.findElement(searchBox).sendKeys(itemName);
        driver.findElement(searchIcon).click();
        return AmazonSearchScreen.get();
    }
}
