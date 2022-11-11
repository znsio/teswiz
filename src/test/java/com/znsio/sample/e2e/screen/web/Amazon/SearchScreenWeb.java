package com.znsio.sample.e2e.screen.web.Amazon;

import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.Amazon.ProductScreen;
import com.znsio.sample.e2e.screen.Amazon.SearchScreen;
import org.openqa.selenium.By;

import java.util.logging.Logger;

public class SearchScreenWeb extends SearchScreen {
    private final Driver driver;
    private final Visual visually;

    private static final String SCREEN_NAME = SearchScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);

    public SearchScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }


    @Override
    public boolean isVisibleItem(String itemName) {
        LOGGER.info(String.format("isVisibleItem - Platform %s : Product list is present", Runner.platform));
        if (driver.findElements(By.xpath("//span[contains(text(),\'" + itemName + "\')]")).size() > 0)
            return true;
        else
            return false;
    }

    @Override
    public ProductScreen navigateToProductDetail(String itemName) {
        LOGGER.info(String.format("navigateToProductDetail - Platform %s : Clicking on first product in list", Runner.platform));
        driver.findElements(By.xpath("//span[contains(text(),\'" + itemName + "\')]")).get(1).click();
        visually.checkWindow(SCREEN_NAME, "Product detail screen");
        driver.switchToNextTab();
        LOGGER.info("Navigated to Product Detail Screen");
        return ProductScreen.get();
    }
}
