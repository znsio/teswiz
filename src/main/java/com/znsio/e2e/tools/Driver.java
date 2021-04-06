package com.znsio.e2e.tools;

import com.znsio.e2e.runner.Runner;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileBy;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.StartsActivity;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static com.znsio.e2e.tools.Wait.waitFor;

public class Driver {
    public static final String WEB_DRIVER = "WebDriver";
    public static final String APPIUM_DRIVER = "AppiumDriver";
    private final String type;
    private final WebDriver driver;
    private Visual visually;
    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

    public Driver (String testName, AppiumDriver<WebElement> appiumDriver) {
        this.driver = appiumDriver;
        this.type = APPIUM_DRIVER;
        instantiateEyes(testName, appiumDriver);
    }

    private void instantiateEyes (String testName, WebDriver innerDriver) {
        this.visually = new Visual(this.type, innerDriver, testName, Runner.isVisualTestingEnabled());
    }

    public Driver (String testName, WebDriver webDriver) {
        this.driver = webDriver;
        this.type = WEB_DRIVER;
        instantiateEyes(testName, webDriver);
    }

    public WebElement waitForVisibilityOf (By elementId) {
        return (new WebDriverWait(driver, 10)).until(ExpectedConditions.elementToBeClickable(elementId));
    }

    public WebElement waitForVisibilityOf (String elementId) {
        return (new WebDriverWait(driver, 10)).until(ExpectedConditions.elementToBeClickable(findElementByAccessibilityId(elementId)));
    }

    public WebElement findElementByAccessibilityId (String locator) {
        return ((AppiumDriver) driver).findElementByAccessibilityId(locator);
    }

    public void waitForAlert () {
        (new WebDriverWait(driver, 10)).until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert();
    }

    public WebElement findElement (By elementId) {
        return driver.findElement(elementId);
    }

    public void hideKeyboard () {
        ((AppiumDriver) driver).hideKeyboard();
    }

    public List<WebElement> findElements (By element) {
        return this.driver.findElements(element);
    }

    public WebElement findElementById (String locator) {
        return driver.findElement(By.id(locator));
    }

    public WebElement findElementByXpath (String locator) {
        return driver.findElement(By.xpath(locator));
    }

    public void scroll (Point fromPoint, Point toPoint) {
        TouchAction touchAction = new TouchAction(((AppiumDriver) driver));
        touchAction.press(PointOption.point(fromPoint))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000)))
                .moveTo(PointOption.point(toPoint))
                .release().perform();
    }

    public WebElement scrollToAnElementByText (String text) {
        return driver.findElement(MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector())" +
                ".scrollIntoView(new UiSelector().text(\"" + text + "\"));"));
    }

    public boolean isElementPresent (By locator) {
        return driver.findElements(locator).size() > 0;
    }

    public boolean isElementPresentWithin (WebElement parentElement, By locator) {
        return parentElement.findElements(locator).size() > 0;
    }

    public void scrollDownByScreenSize () {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        Dimension windowSize = appiumDriver.manage().window().getSize();
        LOGGER.info("dimension: " + windowSize.toString());
        int width = windowSize.width / 2;
        int fromHeight = (int) (windowSize.height * 0.9);
        int toHeight = (int) (windowSize.height * 0.5);
        System.out.printf("width: %s, from height: %s, to height: %s", width, fromHeight, toHeight);

        TouchAction touchAction = new TouchAction(appiumDriver);
        touchAction.press(PointOption.point(new Point(width, fromHeight)))
                .waitAction(WaitOptions.waitOptions(Duration.ofSeconds(1)))
                .moveTo(PointOption.point(new Point(width, toHeight)))
                .release().perform();
    }

    public void tapOnMiddleOfScreen () {
        if (this.type.equals(Driver.APPIUM_DRIVER)) {
            tapOnMiddleOfScreenOnDevice();
        } else {
            simulateMouseMovementOnBrowser();
        }
    }

    private void tapOnMiddleOfScreenOnDevice () {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        Dimension screenSize = appiumDriver.manage().window().getSize();
        int midHeight = screenSize.height / 2;
        int midWidth = screenSize.width / 2;
        System.out.printf("tapOnMiddleOfScreen: Screen dimensions: '%s'. Tapping on coordinates: %d:%d%n", screenSize.toString(), midWidth, midHeight);
        TouchAction touchAction = new TouchAction(appiumDriver);
        touchAction.tap(PointOption.point(midWidth, midHeight)).perform();
        waitFor(1);
    }

    private void simulateMouseMovementOnBrowser () {
        Actions actions = new Actions(this.driver);
        Dimension windowSize = driver.manage().window().getSize();
        int midHeight = windowSize.height / 2;
        int midWidth = windowSize.width / 2;
        // TODO - how can we avoid providing a locator here?
        By byMeetingInfoXpath = By.xpath("//div[@class=\"icon pointer\"]");
        actions.moveToElement(driver.findElement(byMeetingInfoXpath), midHeight, midWidth)
                .perform();
        waitFor(1);
    }

    public void swipeRight () {
        int height = getWindowHeight() / 2;
        int fromWidth = (int) (getWindowWidth() * 0.5);
        int toWidth = (int) (getWindowWidth() * 0.9);
        System.out.printf("height: %s, from width: %s, to width: %s", height, fromWidth, toWidth);

        swipe(height, fromWidth, toWidth);
    }

    private int getWindowHeight () {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        Dimension windowSize = appiumDriver.manage().window().getSize();
        LOGGER.info("dimension: " + windowSize.toString());
        return windowSize.height;
    }

    private int getWindowWidth () {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        return appiumDriver.manage().window().getSize().width;
    }

    private void swipe (int height, int fromWidth, int toWidth) {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        TouchAction touchAction = new TouchAction(appiumDriver);
        touchAction.press(PointOption.point(new Point(fromWidth, height)))
                .waitAction(WaitOptions.waitOptions(Duration.ofSeconds(1)))
                .moveTo(PointOption.point(new Point(toWidth, height)))
                .release().perform();
    }

    public void swipeLeft () {
        int height = getWindowHeight() / 2;
        int fromWidth = (int) (getWindowWidth() * 0.9);
        int toWidth = (int) (getWindowWidth() * 0.5);
        System.out.printf("height: %s, from width: %s, to width: %s", height, fromWidth, toWidth);

        swipe(height, fromWidth, toWidth);
    }

    public void openNotifications () {
        LOGGER.info("Fetching the NOTIFICATIONS on the device: ");
        waitFor(3);
        ((AndroidDriver<WebElement>) driver).openNotifications();
        waitFor(2);
    }

    public void selectNotification (By selectNotificationLocator) {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        WebElement selectNotificationElement = driver.findElement(selectNotificationLocator);
        LOGGER.info("Notification found: " + selectNotificationElement.getText());
        Point notificationCoordinates = selectNotificationElement.getLocation();
        TouchAction touchAction = new TouchAction(appiumDriver);
        touchAction.tap(PointOption.point(notificationCoordinates)).perform();
        LOGGER.info("Tapped on notification. Go back to meeting");
        waitFor(3);
    }

    public void putAppInBackground (int duration) {
        ((AppiumDriver) driver).runAppInBackground(Duration.ofSeconds(duration));
    }

    public void bringAppInForeground () {
        ((StartsActivity) driver).currentActivity();
    }

    public WebDriver getInnerDriver () {
        return driver;
    }

    public String getType () {
        return this.type;
    }

    public Visual getVisual () {
        return this.visually;
    }
}