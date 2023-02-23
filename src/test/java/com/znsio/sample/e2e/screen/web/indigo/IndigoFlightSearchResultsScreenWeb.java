package com.znsio.sample.e2e.screen.web.indigo;

import com.context.TestExecutionContext;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.runner.Driver;
import com.znsio.e2e.runner.Visual;
import com.znsio.sample.e2e.screen.indigo.IndigoFlightSearchResultsScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class IndigoFlightSearchResultsScreenWeb
        extends IndigoFlightSearchResultsScreen {
    private static final String SCREEN_NAME =
            IndigoFlightSearchResultsScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final String NOT_YET_IMPLEMENTED = " not yet implemented";
    private static final By byBackToSearchResultsLinkXpath = By.xpath(
            "//div[@class='bck-to-search']");
    private final Driver driver;
    private final Visual visually;
    private final WebDriver innerDriver;
    private final TestExecutionContext context;

    public IndigoFlightSearchResultsScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        this.innerDriver = this.driver.getInnerDriver();
        long threadId = Thread.currentThread().getId();
        context = Runner.getTestExecutionContext(threadId);
        driver.waitTillElementIsVisible(byBackToSearchResultsLinkXpath, 30);
        visually.checkWindow(SCREEN_NAME, "On Search Results page");
    }
}
