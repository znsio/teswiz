package com.znsio.teswiz.screen.ios.ajio;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.ProductScreen;
import com.znsio.teswiz.screen.ajio.SearchScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

public class SearchScreenIOS
        extends SearchScreen {
    private static final String SCREEN_NAME = SearchScreenIOS.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final By byResultsXpath = By.xpath("//XCUIElementTypeStaticText[@name[contains(.,'Products')]]");
    private static final By byProductXpath = By.xpath("(//XCUIElementTypeButton[@name=\"view_similar_button\"])[1]/preceding-sibling::XCUIElementTypeImage");
    private final Driver driver;
    private final Visual visually;

    public SearchScreenIOS(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public int numberOfProductFound() {
        int numberOfProductFound = Integer.parseInt(
                driver.waitTillElementIsPresent(byResultsXpath).getText().split(" ")[0]);
        visually.checkWindow(SCREEN_NAME, "Result for Image search");
        LOGGER.info("numberOfProductFound: " + numberOfProductFound);
        return numberOfProductFound;
    }

    @Override
    public ProductScreen selectProduct() {
        LOGGER.info("selection of Product in the result page");
        driver.waitTillElementIsPresent(byProductXpath).click();
        return ProductScreen.get();
    }

    @Override
    public boolean isProductListLoaded(String product) {
        return true;
    }
}
