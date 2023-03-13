package com.znsio.teswiz.runner;

import com.google.common.collect.ImmutableMap;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.exceptions.FileNotUploadedException;
import com.znsio.teswiz.tools.Wait;
import io.appium.java_client.*;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.StartsActivity;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.touch.LongPressOptions;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.ElementOption;
import io.appium.java_client.touch.offset.PointOption;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Driver {
    public static final String WEB_DRIVER = "WebDriver";
    public static final String APPIUM_DRIVER = "AppiumDriver";
    private static final Logger LOGGER = Logger.getLogger(Driver.class.getName());
    private final String type;
    private final WebDriver driver;
    private final String userPersona;
    private final String appName;
    private final Platform driverForPlatform;
    private static final String DIMENSION = "dimension: ";
    private static final String FROM_HEIGHT_TO_HEIGHT = "width: %s, from height: %s, to height: %s";
    private static final String TO = "' to '";
    private final boolean isRunningInHeadlessMode;
    private Visual visually;

    Driver(String testName, Platform forPlatform, String userPersona, String appName,
           AppiumDriver<WebElement> appiumDriver) {
        this.driver = appiumDriver;
        this.type = APPIUM_DRIVER;
        this.userPersona = userPersona;
        this.appName = appName;
        this.driverForPlatform = forPlatform;
        this.isRunningInHeadlessMode = false;
        instantiateEyes(testName, appiumDriver);
    }

    Driver(String testName, Platform forPlaform, String userPersona, String appName,
           WebDriver webDriver, boolean isRunInHeadlessMode) {
        this.driver = webDriver;
        this.type = WEB_DRIVER;
        this.userPersona = userPersona;
        this.appName = appName;
        this.driverForPlatform = forPlaform;
        this.isRunningInHeadlessMode = isRunInHeadlessMode;
        instantiateEyes(testName, webDriver);
    }

    private void instantiateEyes(String testName, WebDriver innerDriver) {
        this.visually = new Visual(this.type, this.driverForPlatform, innerDriver, testName,
                                   userPersona, appName);
    }

    public WebElement waitForClickabilityOf(String elementId) {
        return waitForClickabilityOf(elementId, 10);
    }

    public WebElement waitForClickabilityOf(String elementId, int numberOfSecondsToWait) {
        return (new WebDriverWait(driver, numberOfSecondsToWait)).until(
                ExpectedConditions.elementToBeClickable(findElementByAccessibilityId(elementId)));
    }

    public WebElement findElementByAccessibilityId(String locator) {
        return ((AppiumDriver) driver).findElementByAccessibilityId(locator);
    }

    public void waitForAlert() {
        waitForAlert(10);
    }

    public void waitForAlert(int numberOfSecondsToWait) {
        (new WebDriverWait(driver, numberOfSecondsToWait)).until(
                ExpectedConditions.alertIsPresent());
        driver.switchTo().alert();
    }

    public WebElement findElement(By elementId) {
        return driver.findElement(elementId);
    }

    public void hideKeyboard() {
        ((AppiumDriver) driver).hideKeyboard();
    }

    public List<WebElement> findElements(By element) {
        return this.driver.findElements(element);
    }

    public WebElement findElementById(String locator) {
        return driver.findElement(By.id(locator));
    }

    public WebElement findElementByXpath(String locator) {
        return driver.findElement(By.xpath(locator));
    }

    public void scroll(Point fromPoint, Point toPoint) {
        TouchAction touchAction = new TouchAction(((AppiumDriver) driver));
        touchAction.press(PointOption.point(fromPoint))
                   .waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000)))
                   .moveTo(PointOption.point(toPoint)).release().perform();
    }

    public WebElement scrollToAnElementByText(String text) {
        return driver.findElement(MobileBy.AndroidUIAutomator(
                "new UiScrollable(new UiSelector())" + ".scrollIntoView(new UiSelector().text(\"" + text + "\"));"));
    }

    public boolean isElementPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    public boolean isElementPresentByAccessibilityId(String locator) {
        return !((AppiumDriver) driver).findElementsByAccessibilityId(locator).isEmpty();
    }

    public boolean isElementPresentWithin(WebElement parentElement, By locator) {
        return !parentElement.findElements(locator).isEmpty();
    }

    public void scrollDownByScreenSize() {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        Dimension windowSize = appiumDriver.manage().window().getSize();
        LOGGER.info(DIMENSION + windowSize.toString());
        int width = windowSize.width / 2;
        int fromHeight = (int) (windowSize.height * 0.9);
        int toHeight = (int) (windowSize.height * 0.5);
        LOGGER.info(String.format(FROM_HEIGHT_TO_HEIGHT, width, fromHeight, toHeight));

        TouchAction touchAction = new TouchAction(appiumDriver);
        touchAction.press(PointOption.point(new Point(width, fromHeight)))
                   .waitAction(WaitOptions.waitOptions(Duration.ofSeconds(1)))
                   .moveTo(PointOption.point(new Point(width, toHeight))).release().perform();
    }

    public void scrollVertically(int fromPercentScreenHeight, int toPercentScreenHeight,
                                 int percentScreenWidth) {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        Dimension windowSize = appiumDriver.manage().window().getSize();
        LOGGER.info(DIMENSION + windowSize.toString());
        int width = (windowSize.width * percentScreenWidth) / 100;
        int fromHeight = (windowSize.height * fromPercentScreenHeight) / 100;
        int toHeight = (windowSize.height * toPercentScreenHeight) / 100;
        LOGGER.info(String.format(FROM_HEIGHT_TO_HEIGHT, width, fromHeight, toHeight));
        LOGGER.info(String.format(FROM_HEIGHT_TO_HEIGHT, width, fromHeight, toHeight));

        TouchAction touchAction = new TouchAction(appiumDriver);
        touchAction.press(PointOption.point(new Point(width, fromHeight)))
                   .waitAction(WaitOptions.waitOptions(Duration.ofSeconds(1)))
                   .moveTo(PointOption.point(new Point(width, toHeight))).release().perform();
    }

    public void tapOnMiddleOfScreen() {
        if(this.type.equals(Driver.APPIUM_DRIVER)) {
            tapOnMiddleOfScreenOnDevice();
        } else {
            simulateMouseMovementOnBrowser();
        }
    }

    private void tapOnMiddleOfScreenOnDevice() {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        Dimension screenSize = appiumDriver.manage().window().getSize();
        int midHeight = screenSize.height / 2;
        int midWidth = screenSize.width / 2;
        LOGGER.info(String.format(
                "tapOnMiddleOfScreen: Screen dimensions: '%s'. Tapping on coordinates: %d:%d%n",
                screenSize, midWidth, midHeight));
        TouchAction touchAction = new TouchAction(appiumDriver);
        touchAction.tap(PointOption.point(midWidth, midHeight)).perform();
        Wait.waitFor(1);
    }

    private void simulateMouseMovementOnBrowser() {
        Actions actions = new Actions(this.driver);
        Dimension screenSize = driver.manage().window().getSize();
        Point currentPosition = driver.manage().window().getPosition();

        int midHeight = screenSize.height / 2;
        int midWidth = screenSize.width / 2;
        int currentPositionX = currentPosition.getX();
        int currentPositionY = currentPosition.getY();
        LOGGER.info(
                String.format("Current position: '%d':'%d'", currentPositionX, currentPositionY));

        int offsetX = currentPositionX < midWidth ? 50 : -50;
        int offsetY = currentPositionY < midHeight ? 50 : -50;

        LOGGER.info(String.format("Using offset: '%d':'%d'", offsetX, offsetY));

        actions.moveByOffset(offsetX, offsetY).perform();
        Wait.waitFor(1);
    }

    public void swipeRight() {
        int height = getWindowHeight() / 2;
        int fromWidth = (int) (getWindowWidth() * 0.5);
        int toWidth = (int) (getWindowWidth() * 0.9);
        LOGGER.info("height: " + height + ", from width: " + fromWidth + ", to width: " + toWidth);
        swipe(height, fromWidth, toWidth);
    }

    private int getWindowHeight() {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        Dimension windowSize = appiumDriver.manage().window().getSize();
        LOGGER.info(DIMENSION + windowSize.toString());
        return windowSize.height;
    }

    private int getWindowWidth() {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        return appiumDriver.manage().window().getSize().width;
    }

    private void swipe(int height, int fromWidth, int toWidth) {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        TouchAction touchAction = new TouchAction(appiumDriver);
        touchAction.press(PointOption.point(new Point(fromWidth, height)))
                   .waitAction(WaitOptions.waitOptions(Duration.ofSeconds(1)))
                   .moveTo(PointOption.point(new Point(toWidth, height))).release().perform();
    }

    public void swipeLeft() {
        int height = getWindowHeight() / 2;
        int fromWidth = (int) (getWindowWidth() * 0.9);
        int toWidth = (int) (getWindowWidth() * 0.5);
        LOGGER.info(String.format("height: %s, from width: %s, to width: %s", height, fromWidth,
                                  toWidth));
        swipe(height, fromWidth, toWidth);
    }

    public void openNotifications() {
        LOGGER.info("Fetching the NOTIFICATIONS on the device: ");
        Wait.waitFor(3);
        ((AndroidDriver<WebElement>) driver).openNotifications();
        Wait.waitFor(2);
    }

    public void selectNotification(By selectNotificationLocator) {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        WebElement selectNotificationElement = driver.findElement(selectNotificationLocator);
        LOGGER.info("Notification found: " + selectNotificationElement.isDisplayed());
        Point notificationCoordinates = selectNotificationElement.getLocation();

        TouchAction touchAction = new TouchAction(appiumDriver);
        touchAction.tap(PointOption.point(notificationCoordinates)).perform();
        LOGGER.info("Tapped on notification. Go back to meeting");
        Wait.waitFor(3);
    }

    public void putAppInBackground(int duration) {
        ((AppiumDriver) driver).runAppInBackground(Duration.ofSeconds(duration));
    }

    public void bringAppInForeground() {
        ((StartsActivity) driver).currentActivity();
    }

    public void goToDeepLinkUrl(String url, String packageName) {
        LOGGER.info("Hitting a Deep Link URL: " + url);
        ((AppiumDriver) driver).executeScript("mobile:deepLink",
                                              ImmutableMap.of("url", url, "package", packageName));
    }

    public WebDriver getInnerDriver() {
        return driver;
    }

    public String getType() {
        return this.type;
    }

    public Visual getVisual() {
        return this.visually;
    }

    public void longPress(By elementId) {
        MobileElement elementToBeLongTapped = (MobileElement) new WebDriverWait(driver, 10).until(
                ExpectedConditions.elementToBeClickable(elementId));

        TouchAction action = new TouchAction((PerformsTouchActions) driver);
        action.longPress(LongPressOptions.longPressOptions()
                                         .withElement(ElementOption.element(elementToBeLongTapped)))
              .release().perform();
    }

    public void pushFileToDevice(String filePathToPush, String devicePath) {
        LOGGER.info("Pushing the file: '" + filePathToPush + TO + Runner.getPlatform()
                                                                        .name() + "' " + "device "
                    + "on path: '" + devicePath + "'");
        try {
            if(Runner.getPlatform().equals(Platform.android)) {
                ((AndroidDriver) driver).pushFile(devicePath, new File(filePathToPush));
            } else if(Runner.getPlatform().equals(Platform.iOS)) {
                ((IOSDriver) driver).pushFile(devicePath, new File(filePathToPush));
            }
        } catch(IOException e) {
            throw new FileNotUploadedException(
                    String.format("Error in pushing the file: '%s%s%s' device on path: '%s'",
                                  filePathToPush, TO, Runner.getPlatform().name(), devicePath), e);
        }
    }

    public void allowPermission(By element) {
        waitForClickabilityOf(element);
        if(Runner.getPlatform().equals(Platform.android)) {
            driver.findElement(element).click();
        }
    }

    public WebElement waitForClickabilityOf(By elementId) {
        return waitForClickabilityOf(elementId, 10);
    }

    public WebElement waitForClickabilityOf(By elementId, int numberOfSecondsToWait) {
        return (new WebDriverWait(driver, numberOfSecondsToWait)).until(
                ExpectedConditions.elementToBeClickable(elementId));
    }

    public List<WebElement> findElementsByAccessibilityId(String elementId) {
        return ((AppiumDriver) driver).findElementsByAccessibilityId(elementId);
    }

    public WebElement waitTillElementIsPresent(By elementId) {
        return waitTillElementIsPresent(elementId, 10);
    }

    public WebElement waitTillElementIsPresent(By elementId, int numberOfSecondsToWait) {
        return (new WebDriverWait(driver, numberOfSecondsToWait)).until(
                ExpectedConditions.presenceOfElementLocated(elementId));
    }

    public WebElement waitTillElementIsVisible(By elementId) {
        return waitTillElementIsVisible(elementId, 10);
    }

    public WebElement waitTillElementIsVisible(By elementId, int numberOfSecondsToWait) {
        return (new WebDriverWait(driver, numberOfSecondsToWait)).until(
                ExpectedConditions.visibilityOfElementLocated(elementId));
    }

    public List<WebElement> waitTillVisibilityOfAllElements(By elementId) {
        return waitTillVisibilityOfAllElements(elementId, 10);
    }

    public List<WebElement> waitTillVisibilityOfAllElements(By elementId,
                                                            int numberOfSecondsToWait) {
        return (new WebDriverWait(driver, numberOfSecondsToWait)).until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(elementId));
    }

    public WebElement waitTillElementIsVisible(String elementId) {
        return waitTillElementIsVisible(elementId, 10);
    }

    public WebElement waitTillElementIsVisible(String elementId, int numberOfSecondsToWait) {
        return (new WebDriverWait(driver, numberOfSecondsToWait)).until(
                ExpectedConditions.visibilityOf(findElementByAccessibilityId(elementId)));
    }

    public List<WebElement> waitTillPresenceOfAllElements(By elementId) {
        return waitTillPresenceOfAllElements(elementId, 10);
    }

    public List<WebElement> waitTillPresenceOfAllElements(By elementId, int numberOfSecondsToWait) {
        return (new WebDriverWait(driver, numberOfSecondsToWait)).until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(elementId));
    }

    public void setWindowSize(int width, int height) {
        if(this.type.equals(Driver.WEB_DRIVER)) {
            driver.manage().window().setSize(new Dimension(width, height));
        }
    }

    public void moveToElement(By moveToElementLocator) {
        Actions actions = new Actions(driver);
        actions.moveToElement(driver.findElement(moveToElementLocator)).build().perform();
        Wait.waitFor(1);
    }

    public boolean isDriverRunningInHeadlessMode() {
        return this.isRunningInHeadlessMode;
    }

    public WebDriver setWebViewContext() {
        AppiumDriver<WebElement> appiumDriver = (AppiumDriver<WebElement>) driver;
        Set<String> contextNames = appiumDriver.getContextHandles();
        return appiumDriver.context((String) contextNames.toArray()[contextNames.size() - 1]);
    }

    public WebDriver setNativeAppContext() {
        return setNativeAppContext("NATIVE_APP");
    }

    public WebDriver setNativeAppContext(String contextName) {
        AppiumDriver<WebElement> appiumDriver = (AppiumDriver<WebElement>) driver;
        return appiumDriver.context(contextName);
    }

    public WebDriver switchFrameToDefault() {
        return driver.switchTo().defaultContent();
    }

    public WebDriver switchToFrame(String id) {
        return driver.switchTo().frame(id);
    }

    public void scrollToBottom() {
        ((JavascriptExecutor) driver).executeScript(
                "window.scrollTo(0, document.body.scrollHeight)");
    }

    public void scrollTillElementIntoView(By elementId) {
        WebElement element = driver.findElement(elementId);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public void switchToNextTab() {
        Iterator<String> iterator = driver.getWindowHandles().iterator();
        try {
            iterator.next();
            driver.switchTo().window(iterator.next());
        } catch(NoSuchElementException e) {
            throw new NoSuchElementException("Unable to get next window handle.", e);
        }
    }

    public void switchToParentTab() {
        try {
            driver.switchTo().window(driver.getWindowHandles().iterator().next());
        } catch(NoSuchElementException e) {
            throw new NoSuchElementException("No previous tab found.", e);
        }
    }

    public void uploadFileInBrowser(String filePath, By locator) {
        try {
            LOGGER.info("Uploading file: " + filePath + " to the browser");
            driver.findElement(locator).sendKeys(filePath);
        } catch(Exception e) {
            throw new FileNotUploadedException(
                    String.format("Error in uploading the file: '%s%s%s", filePath, TO,
                                  Runner.getPlatform().name()), e);
        }
    }

    /**
     * This method injects the media to browserstack to perform,
     * image scanning eg: QRcode,barcode etc
     * Throws NotImplementedException if platform is NOT android, and cloudName is NOT browserstack
     *
     * @param uploadFileURL
     */
    public void injectMediaToBrowserstackDevice(String uploadFileURL) {
        String cloudName = Runner.getCloudName();
        if(Runner.getPlatform().equals(Platform.android) && cloudName.equalsIgnoreCase(
                "browserstack")) {
            String cloudUser = Runner.getCloudUser();
            String cloudKey = Runner.getCloudKey();
            BrowserStackImageInjection.injectMediaToDriver(uploadFileURL, ((AppiumDriver) driver),
                                                           cloudUser, cloudKey);
        } else {
            throw new NotImplementedException(
                    "injectMediaToBrowserstackDevice is not implemented for: " + cloudName);
        }
    }

    public static void scrollInDynamicLayer(Driver driver, String direction) {
        Dimension dimension = driver.getInnerDriver().manage().window().getSize();
        int width = (int) (dimension.width * 0.5);
        int fromHeight = (int) (dimension.height * 0.7), toHeight = (int) (dimension.height * 0.6);
        int[] height = {fromHeight, toHeight};
        if (direction.equalsIgnoreCase("up"))
            Arrays.sort(height);

        TouchAction<?> touchAction = new TouchAction<>((PerformsTouchActions) driver.getInnerDriver());
        touchAction.press(PointOption.point(width, height[0]))
                .waitAction(WaitOptions.waitOptions(Duration.ofSeconds(1)))
                .moveTo(PointOption.point(width, height[1])).release().perform();
    }
}