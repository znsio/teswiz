package com.znsio.teswiz.screen.ios.theapp;

import com.applitools.eyes.appium.Target;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.theapp.LoginScreen;
import io.appium.java_client.AppiumBy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.znsio.teswiz.tools.Wait.waitFor;

public class LoginScreenIOS
        extends LoginScreen {
    private final Driver driver;
    private final Visual visually;
    private static final String SCREEN_NAME = LoginScreenIOS.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);
    private static final String INVALID_LOGIN_TEXT = "Invalid login credentials";
    private static final int ELEMENT_TIMEOUT_SECONDS = 8;
    private final By byUserNameId = AppiumBy.accessibilityId("username");
    private final By byUserNameClassChain = AppiumBy.iOSClassChain(
            "**/XCUIElementTypeTextField[`name == 'username' OR label == 'username' OR value == 'username'`]");
    private final By byPasswordId = AppiumBy.accessibilityId("password");
    private final By byPasswordClassChain = AppiumBy.iOSClassChain(
            "**/XCUIElementTypeSecureTextField[`name == 'password' OR label == 'password' OR value == 'password'`]");
    private final By byLoginButtonId = AppiumBy.accessibilityId("loginBtn");
    private final By byLoginButtonClassChain = AppiumBy.iOSClassChain(
            "**/XCUIElementTypeOther[`name == 'loginBtn' AND visible == 1`]");
    private final By byLoginButtonPredicate = AppiumBy.iOSNsPredicateString(
            "(type == 'XCUIElementTypeButton' OR type == 'XCUIElementTypeOther') AND " +
            "(name == 'loginBtn' OR label == 'loginBtn') AND visible == 1");
    private final By byLoginButtonLastXpath = AppiumBy.xpath("(//XCUIElementTypeOther[@name='loginBtn' or @label='loginBtn'])[last()]");
    private final By byKeyboardClassChain = AppiumBy.iOSClassChain("**/XCUIElementTypeKeyboard");
    private final By byOKButtonPredicate = AppiumBy.iOSNsPredicateString(
            "type == 'XCUIElementTypeButton' AND (name == 'OK' OR label == 'OK' OR name == 'Ok' OR label == 'Ok' OR name == 'Dismiss' OR label == 'Dismiss')");
    private final By byAlertErrorTextClassChain = AppiumBy.iOSClassChain(
            "**/XCUIElementTypeAlert/**/XCUIElementTypeStaticText[`name CONTAINS[c] '" + INVALID_LOGIN_TEXT + "' OR label CONTAINS[c] '" + INVALID_LOGIN_TEXT + "' OR value CONTAINS[c] '" + INVALID_LOGIN_TEXT + "'`]");
    private final By byErrorTextPredicate = AppiumBy.iOSNsPredicateString(
            "(type == 'XCUIElementTypeStaticText' OR type == 'XCUIElementTypeOther') AND " +
            "(name CONTAINS[c] '" + INVALID_LOGIN_TEXT + "' OR label CONTAINS[c] '" + INVALID_LOGIN_TEXT + "' OR value CONTAINS[c] '" + INVALID_LOGIN_TEXT + "')");

    public LoginScreenIOS(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public LoginScreen enterLoginDetails(String username, String password) {
        waitFor(2);
        WebElement userNameElement = getFirstVisibleElement(byUserNameId, byUserNameClassChain);
        userNameElement.clear();
        userNameElement.sendKeys(username);
        WebElement passwordElement = getFirstVisibleElement(byPasswordId, byPasswordClassChain);
        passwordElement.clear();
        passwordElement.sendKeys(password);
        dismissKeyboardIfPresent();
        //        driver.waitForVisibilityOf(passwordId).sendKeys(username);
        visually.check(SCREEN_NAME, "Entered login details",
                       Target.window().fully().layout(userNameElement, passwordElement));
        return this;
    }

    @Override
    public LoginScreen login() {
        waitFor(1);
        dismissKeyboardIfPresent();
        clickLoginButton();
        if (isKeyboardVisible()) {
            // First tap can dismiss keyboard on iOS. Retry submit after hiding it.
            dismissKeyboardIfPresent();
            clickLoginButton();
        }
        waitFor(2);
        visually.checkWindow(SCREEN_NAME, "Clicked on Login");
        return this;
    }

    @Override
    public String getInvalidLoginError() {
        WebElement alertTextElement = getFirstVisibleElement(5, byAlertErrorTextClassChain, byErrorTextPredicate);
        if (alertTextElement != null) {
            String alertText = alertTextElement.getText();
            LOGGER.info("actualAlertText (via iOS locator): " + alertText);
            return alertText;
        }

        try {
            driver.waitForAlert(2);
            Alert alert = driver.getInnerDriver().switchTo().alert();
            String alertText = alert.getText();
            LOGGER.info("actualAlertText (via native alert): " + alertText);
            return alertText;
        } catch (NoAlertPresentException | TimeoutException ignored) {
            String pageSource = driver.getInnerDriver().getPageSource();
            if (pageSource.contains(INVALID_LOGIN_TEXT)) {
                LOGGER.info("actualAlertText inferred from page source");
                return "Invalid login credentials, please try again";
            }
            throw new TimeoutException(
                    "Could not locate iOS invalid-login message using any supported locator");
        }
    }

    @Override
    public LoginScreen dismissAlert() {
        try {
            driver.waitForAlert(3);
            Alert alert = driver.getInnerDriver().switchTo().alert();
            alert.accept();
        } catch (NoAlertPresentException | TimeoutException ignored) {
            WebElement dismissButton = getFirstVisibleElement(3, byOKButtonPredicate);
            if (dismissButton != null) {
                dismissButton.click();
            }
        }
        return this;
    }

    private WebElement getFirstVisibleElement(int timeoutInSeconds, By... locators) {
        Instant timeoutAt = Instant.now().plus(Duration.ofSeconds(timeoutInSeconds));
        do {
            for (By locator : locators) {
                WebElement visibleElement = getVisibleElementFor(locator);
                if (visibleElement != null) {
                    return visibleElement;
                }
            }
            waitFor(1);
        } while (Instant.now().isBefore(timeoutAt));

        for (By locator : locators) {
            LOGGER.info("Locator not visible yet: " + locator);
        }
        return null;
    }

    private WebElement getVisibleElementFor(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        for (WebElement element : elements) {
            try {
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (StaleElementReferenceException ignored) {
                // Query the locator again on the next polling iteration.
            }
        }
        return null;
    }

    private WebElement getFirstVisibleElement(By... locators) {
        WebElement element = getFirstVisibleElement(ELEMENT_TIMEOUT_SECONDS, locators);
        if (element != null) {
            return element;
        }
        for (By locator : locators) {
            LOGGER.info("Unable to find visible element using locator: " + locator);
        }
        return driver.findElement(locators[0]);
    }

    private void clickFirstVisibleElement(By... locators) {
        WebElement elementToClick = getFirstVisibleElement(locators);
        try {
            elementToClick.click();
        } catch (WebDriverException clickException) {
            LOGGER.info("Regular click failed for element. Falling back to mobile tap. Error: " +
                        clickException.getMessage());
            tapElementCenter(elementToClick);
        }
    }

    private void clickLoginButton() {
        WebElement loginButton = getMostLikelyLoginButton();
        if (loginButton == null) {
            clickFirstVisibleElement(byLoginButtonLastXpath, byLoginButtonId, byLoginButtonClassChain,
                                     byLoginButtonPredicate);
            return;
        }
        try {
            loginButton.click();
        } catch (WebDriverException clickException) {
            LOGGER.info("Click on login button failed. Falling back to center tap. Error: " +
                        clickException.getMessage());
            tapElementCenter(loginButton);
        }
    }

    private WebElement getMostLikelyLoginButton() {
        Set<WebElement> candidates = new LinkedHashSet<>();
        candidates.addAll(driver.findElements(byLoginButtonLastXpath));
        candidates.addAll(driver.findElements(byLoginButtonId));
        candidates.addAll(driver.findElements(byLoginButtonClassChain));
        candidates.addAll(driver.findElements(byLoginButtonPredicate));

        return candidates.stream()
                .filter(WebElement::isDisplayed)
                // Inner clickable element is typically the smallest visible loginBtn node.
                .min(Comparator.comparingInt(element -> element.getRect().getWidth() * element.getRect().getHeight()))
                .orElse(null);
    }

    private void tapElementCenter(WebElement element) {
        Map<String, Object> params = new HashMap<>();
        int tapX = element.getRect().getX() + (element.getRect().getWidth() / 2);
        int tapY = element.getRect().getY() + (element.getRect().getHeight() / 2);
        params.put("x", tapX);
        params.put("y", tapY);
        ((JavascriptExecutor) driver.getInnerDriver()).executeScript("mobile: tap", params);
    }

    private void dismissKeyboardIfPresent() {
        try {
            driver.hideKeyboard();
            waitFor(1);
            if (!isKeyboardVisible()) {
                return;
            }
        } catch (Exception ignored) {
            // Continue with fallback strategy below.
        }
        try {
            List<WebElement> returnButtons = driver.findElements(AppiumBy.accessibilityId("return"));
            if (!returnButtons.isEmpty()) {
                returnButtons.get(0).click();
                waitFor(1);
                return;
            }
            List<WebElement> returnButtonsUpper = driver.findElements(AppiumBy.accessibilityId("Return"));
            if (!returnButtonsUpper.isEmpty()) {
                returnButtonsUpper.get(0).click();
                waitFor(1);
            }
        } catch (Exception ignored) {
            // Keep login flow moving even if keyboard is not present.
        }
    }

    private boolean isKeyboardVisible() {
        try {
            return !driver.findElements(byKeyboardClassChain).isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }
}
