package com.znsio.teswiz.screen.web.playwrightjava.indigo;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.indigo.IndigoFlightSearchResultsScreen;
import com.znsio.teswiz.screen.indigo.IndigoGiftVouchersScreen;
import com.znsio.teswiz.screen.indigo.IndigoHomeScreen;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class IndigoHomeScreenPlaywrightJava extends IndigoHomeScreen {
    private static final String SCREEN_NAME = IndigoHomeScreenPlaywrightJava.class.getSimpleName();

    private static final By CLOSE_COOKIE_BANNER = By.cssSelector("a.close-cookie");
    private static final By FROM_INPUT = By.xpath("//input[@placeholder='From']");
    private static final By TO_INPUT = By.xpath("//input[@placeholder='To']");
    private static final By POPULAR_DESTINATIONS = By.xpath("..//div[text()=\"Popular Destinations\"]");
    private static final By PASSENGER_INPUT = By.xpath("//input[@name='passenger']");
    private static final By JOURNEY_TYPE_DROPDOWN = By.xpath("//div[@class='filter-option-inner-inner']");
    private static final By ONE_WAY_JOURNEY_TYPE = By.xpath("//a[contains(@class,'one-way-tab')]");
    private static final By EXTRA_SEAT_TOOLTIP_CLOSE = By.xpath("//i[@class='icon-close close-extraseat-tooltip']");
    private static final By PASSENGER_SELECTION_DONE = By.xpath("//button[text()='Done']");
    private static final By DECREASE_ADULT_PASSENGER_COUNT = By.xpath("//button[@title='Decrease Adult Passenger Count']");
    private static final By INCREASE_ADULT_PASSENGER_COUNT = By.xpath("//button[@title='Increase Adult Passenger Count']");
    private static final By ADULT_PASSENGER_COUNT = By.xpath("//input[@class='counter adult-pax']");
    private static final By SEARCH_FLIGHT_BUTTON = By.xpath("//span[text()='Search Flight']");
    private static final By BOOK_MENU = By.xpath("//a[@title='Book']");
    private static final By GIFT_VOUCHER_MENU = By.xpath("//div[@class='menu-wrapper-child']//div[text()='Gift Voucher']");

    private final Driver driver;
    private final Visual visually;

    public IndigoHomeScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        dismissCookieBannerIfPresent();
    }

    @Override
    public IndigoHomeScreen selectFrom(String from) {
        WebElement fromInput = driver.findElement(FROM_INPUT);
        fromInput.click();
        fromInput.sendKeys(from);
        fromInput.findElement(By.xpath(fromOptionLocator(from))).click();
        visually.checkWindow(SCREEN_NAME, "selected from");
        return this;
    }

    @Override
    public IndigoHomeScreen selectTo(String destination) {
        WebElement toInput = driver.findElement(TO_INPUT);
        toInput.click();
        toInput.sendKeys(destination);
        toInput.findElement(POPULAR_DESTINATIONS);
        toInput.findElement(By.xpath(toOptionLocator(destination))).click();
        visually.checkWindow(SCREEN_NAME, "selected destination");
        return this;
    }

    @Override
    public IndigoHomeScreen selectNumberOfAdultPassengers(int numberOfAdultsToSelect) {
        driver.waitForClickabilityOf(PASSENGER_INPUT).click();
        dismissExtraSeatTooltipIfPresent();
        updateAdultPassengerCount(numberOfAdultsToSelect);
        driver.findElement(PASSENGER_SELECTION_DONE).click();
        return this;
    }

    @Override
    public IndigoHomeScreen selectJourneyType(String journeyType) {
        driver.findElement(JOURNEY_TYPE_DROPDOWN).click();
        visually.checkWindow(SCREEN_NAME, "Journey type options displayed");
        driver.findElement(ONE_WAY_JOURNEY_TYPE).click();
        return this;
    }

    @Override
    public IndigoFlightSearchResultsScreen searchFlightOptions() {
        driver.findElement(SEARCH_FLIGHT_BUTTON).click();
        return IndigoFlightSearchResultsScreen.get();
    }

    @Override
    public IndigoGiftVouchersScreen selectGiftVouchers() {
        Actions actions = new Actions(driver.getInnerDriver());
        actions.moveToElement(driver.findElement(BOOK_MENU))
                .moveToElement(driver.findElement(GIFT_VOUCHER_MENU))
                .click()
                .build()
                .perform();
        visually.checkWindow(SCREEN_NAME, "Clicked on Gift Voucher");
        return IndigoGiftVouchersScreen.get();
    }

    private void dismissCookieBannerIfPresent() {
        if (driver.isElementPresent(CLOSE_COOKIE_BANNER)) {
            driver.findElement(CLOSE_COOKIE_BANNER).click();
        }
    }

    private void dismissExtraSeatTooltipIfPresent() {
        if (driver.isElementPresent(EXTRA_SEAT_TOOLTIP_CLOSE)) {
            driver.findElement(EXTRA_SEAT_TOOLTIP_CLOSE).click();
        }
    }

    private void updateAdultPassengerCount(int numberOfAdultsToSelect) {
        int numberOfAdultsSelected = getSelectedAdultPassengerCount();
        while (numberOfAdultsToSelect < numberOfAdultsSelected && numberOfAdultsSelected != 1) {
            driver.findElement(DECREASE_ADULT_PASSENGER_COUNT).click();
            numberOfAdultsSelected -= 1;
        }
        while (numberOfAdultsToSelect > numberOfAdultsSelected) {
            driver.findElement(INCREASE_ADULT_PASSENGER_COUNT).click();
            numberOfAdultsSelected += 1;
        }
    }

    private int getSelectedAdultPassengerCount() {
        return Integer.parseInt(driver.findElement(ADULT_PASSENGER_COUNT).getAttribute("value"));
    }

    private String fromOptionLocator(String from) {
        return String.format("..//div[@data-name='%s']", from);
    }

    private String toOptionLocator(String destination) {
        return String.format("..//div[@data-name='%s']", destination);
    }
}
