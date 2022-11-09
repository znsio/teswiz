package com.znsio.sample.e2e.screen.web.Amazon;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.Amazon.AmazonProductScreen;
import com.znsio.sample.e2e.screen.Amazon.AmazonSearchScreen;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.logging.Logger;

public class AmazonSearchScreenWeb extends AmazonSearchScreen {
    private final Driver driver;
    private final Visual visually;

    private static final String SCREEN_NAME = AmazonSearchScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);

    public AmazonSearchScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }


    @Override
    public boolean isVisibleItem(String itemName) {
        LOGGER.info("Product list is present");
        if (driver.findElements(By.xpath("//span[contains(text(),\'" + itemName + "\')]")).size() > 0)
            return true;
        else
            return false;
    }

    @Override
    public AmazonProductScreen navigateToProductDetail(String itemName) {
        LOGGER.info("Clicking on first product in list");
        driver.findElements(By.xpath("//span[contains(text(),\'" + itemName + "\')]")).get(1).click();
        visually.checkWindow(SCREEN_NAME, "Product detail screen");
        driver.switchToNextTab();
        LOGGER.info("Navigated to Product Detail Screen");
        return AmazonProductScreen.get();
    }
}
