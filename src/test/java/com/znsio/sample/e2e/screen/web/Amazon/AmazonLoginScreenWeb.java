package com.znsio.sample.e2e.screen.web.Amazon;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.Amazon.AmazonHomeScreen;
import com.znsio.sample.e2e.screen.Amazon.AmazonLoginScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

public class AmazonLoginScreenWeb extends AmazonLoginScreen {

    private final Driver driver;
    private final Visual visually;
    private static final String SCREEN_NAME = AmazonLoginScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);

    private static final By signInAccount = By.id("nav-link-accountList");
    private static final By userName = By.id("ap_email");
    private static final By continueButton = By.id("continue");
    private static final By userPassword = By.id("ap_password");
    private static final By signInButton = By.id("signInSubmit");

    public AmazonLoginScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    /**
     * Utility to login with given parameters
     * @return {@link AmazonHomeScreen}
     */
    @Override
    public AmazonHomeScreen loginWithCredentials(String userName, String password) {

        LOGGER.info("Logging with the credentials");
        driver.findElement(signInAccount).click();

        LOGGER.info("Clicking on Username Field");
        driver.findElement(AmazonLoginScreenWeb.userName).click();

        LOGGER.info("Enter username");
        driver.findElement(AmazonLoginScreenWeb.userName).sendKeys(userName);
        driver.findElement(continueButton).click();

        LOGGER.info("Enter password");
        driver.findElement(userPassword).sendKeys(password);
        driver.findElement(signInButton).click();

        LOGGER.info("Have successfully login to amazon");
        return AmazonHomeScreen.get();
    }
}
