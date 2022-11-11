package com.znsio.sample.e2e.screen.web.Amazon;

import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.Amazon.HomeScreen;
import com.znsio.sample.e2e.screen.Amazon.LoginScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

public class LoginScreenWeb extends LoginScreen {

    private final Driver driver;
    private final Visual visually;
    private static final String SCREEN_NAME = LoginScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);

    private static final By signInAccount = By.id("nav-link-accountList");
    private static final By userName = By.id("ap_email");
    private static final By continueButton = By.id("continue");
    private static final By userPassword = By.id("ap_password");
    private static final By signInButton = By.id("signInSubmit");

    public LoginScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    /**
     * Method to login with given parameters
     * @return {@link HomeScreen}
     */
    @Override
    public HomeScreen loginWithCredentials(String userName, String password) {
        LOGGER.info(String.format("loginWithCredentials - Platform %s : Logging with the credentials", Runner.platform));
        driver.findElement(signInAccount).click();

        LOGGER.info("Clicking on Username Field");
        driver.findElement(LoginScreenWeb.userName).click();

        LOGGER.info("Enter username");
        driver.findElement(LoginScreenWeb.userName).sendKeys(userName);
        driver.findElement(continueButton).click();

        LOGGER.info("Enter password");
        driver.findElement(userPassword).sendKeys(password);
        driver.findElement(signInButton).click();

        LOGGER.info("Have successfully login to amazon");
        return HomeScreen.get();
    }
}
