package com.znsio.sample.e2e.screen.web.Amazon;

import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.Amazon.HomeScreen;
import com.znsio.sample.e2e.screen.Amazon.SearchScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

public class HomeScreenWeb extends HomeScreen {

    private final Driver driver;
    private final Visual visually;
    private static final String SCREEN_NAME = HomeScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);

    public static final By searchBox = By.id("twotabsearchtextbox");
    public static final By searchIcon = By.id("nav-search-submit-button");

    public HomeScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    /**
     * Method for searching the item in searchbox
     * @param itemName
     * @return
     */
    @Override
    public SearchScreen searchItem(String itemName) {
        LOGGER.info(String.format("searchItem - Platform %s : Searching for product", Runner.platform));
        driver.waitForClickabilityOf(searchBox).click();
        driver.findElement(searchBox).sendKeys(itemName);
        driver.findElement(searchIcon).click();
        return SearchScreen.get();
    }
}
