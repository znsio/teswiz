package com.znsio.sample.e2e.screen.android.ajio;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.ajio.SearchScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class SearchScreenAndroid extends SearchScreen {
    private final Driver driver;
    private final Visual visually;
    private static final String SCREEN_NAME = SearchScreenAndroid.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final By byResultsId = By.id("com.ril.ajio:id/tv_count_plp_header_is");
    private static final By byProductId = By.id("com.ril.ajio:id/plp_row_product_iv");

    public SearchScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public int numberOfProductFound() {
        int numberOfProductFound = Integer.parseInt(driver.waitTillElementIsPresent(byResultsId).getText().split(" ")[0]);
        visually.checkWindow(SCREEN_NAME,"Result for Image search");
        LOGGER.info("numberOfProductFound: "+numberOfProductFound);
        return numberOfProductFound;
    }

    @Override
    public void selectProduct(){
        LOGGER.info("selection of Product in the result page");
        driver.waitTillElementIsPresent(By.id("com.ril.ajio:id/layout_category_container")).click();
        List<WebElement> list= driver.waitTillPresenceOfAllElements(byProductId);
        list.get(0).click();
    }
}
