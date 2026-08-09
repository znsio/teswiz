package com.znsio.teswiz.screen.web.playwrightjava.indigo;

import com.microsoft.playwright.Locator;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.indigo.IndigoFlightSearchResultsScreen;
import com.znsio.teswiz.screen.indigo.IndigoGiftVouchersScreen;
import com.znsio.teswiz.screen.indigo.IndigoHomeScreen;
import com.znsio.teswiz.web.playwright.screen.PlaywrightJavaScreenContext;

public class IndigoHomeScreenPlaywrightJava extends IndigoHomeScreen {
    private static final String SCREEN_NAME = IndigoHomeScreenPlaywrightJava.class.getSimpleName();

    private static final String CLOSE_COOKIE_BANNER = "a.close-cookie";
    private static final String FROM_INPUT = "input[placeholder='From']";
    private static final String TO_INPUT = "input[placeholder='To']";
    private static final String PASSENGER_INPUT = "input[name='passenger']";
    private static final String JOURNEY_TYPE_DROPDOWN = "div.filter-option-inner-inner";
    private static final String ONE_WAY_JOURNEY_TYPE = "a.one-way-tab";
    private static final String EXTRA_SEAT_TOOLTIP_CLOSE = "i.icon-close.close-extraseat-tooltip";
    private static final String PASSENGER_SELECTION_DONE = "button:has-text(\"Done\")";
    private static final String DECREASE_ADULT_PASSENGER_COUNT = "button[title='Decrease Adult Passenger Count']";
    private static final String INCREASE_ADULT_PASSENGER_COUNT = "button[title='Increase Adult Passenger Count']";
    private static final String ADULT_PASSENGER_COUNT = "input.counter.adult-pax";
    private static final String SEARCH_FLIGHT_BUTTON = "span:has-text(\"Search Flight\")";
    private static final String BOOK_MENU = "a[title='Book']";
    private static final String GIFT_VOUCHER_MENU = "div.menu-wrapper-child div:has-text(\"Gift Voucher\")";

    private final PlaywrightJavaScreenContext context;
    private final Visual visually;

    public IndigoHomeScreenPlaywrightJava(PlaywrightJavaScreenContext context) {
        this.context = context;
        this.visually = context.visual();
        dismissCookieBannerIfPresent();
    }

    @Override
    public IndigoHomeScreen selectFrom(String from) {
        Locator fromInput = context.page().locator(FROM_INPUT);
        fromInput.click();
        fromInput.fill(from);
        fromInput.locator("xpath=..//div[@data-name='" + from + "']").click();
        visually.checkWindow(SCREEN_NAME, "selected from");
        return this;
    }

    @Override
    public IndigoHomeScreen selectTo(String destination) {
        Locator toInput = context.page().locator(TO_INPUT);
        toInput.click();
        toInput.fill(destination);
        toInput.locator("xpath=..//div[@data-name='" + destination + "']").click();
        visually.checkWindow(SCREEN_NAME, "selected destination");
        return this;
    }

    @Override
    public IndigoHomeScreen selectNumberOfAdultPassengers(int numberOfAdultsToSelect) {
        context.page().locator(PASSENGER_INPUT).click();
        dismissExtraSeatTooltipIfPresent();
        updateAdultPassengerCount(numberOfAdultsToSelect);
        context.page().locator(PASSENGER_SELECTION_DONE).click();
        return this;
    }

    @Override
    public IndigoHomeScreen selectJourneyType(String journeyType) {
        context.page().locator(JOURNEY_TYPE_DROPDOWN).click();
        visually.checkWindow(SCREEN_NAME, "Journey type options displayed");
        context.page().locator(ONE_WAY_JOURNEY_TYPE).click();
        return this;
    }

    @Override
    public IndigoFlightSearchResultsScreen searchFlightOptions() {
        context.page().locator(SEARCH_FLIGHT_BUTTON).click();
        return IndigoFlightSearchResultsScreen.get();
    }

    @Override
    public IndigoGiftVouchersScreen selectGiftVouchers() {
        context.page().locator(BOOK_MENU).hover();
        context.page().locator(GIFT_VOUCHER_MENU).click();
        visually.checkWindow(SCREEN_NAME, "Clicked on Gift Voucher");
        return IndigoGiftVouchersScreen.get();
    }

    private void dismissCookieBannerIfPresent() {
        Locator closeButton = context.page().locator(CLOSE_COOKIE_BANNER);
        if (closeButton.isVisible()) {
            closeButton.click();
        }
    }

    private void dismissExtraSeatTooltipIfPresent() {
        Locator closeButton = context.page().locator(EXTRA_SEAT_TOOLTIP_CLOSE);
        if (closeButton.isVisible()) {
            closeButton.click();
        }
    }

    private void updateAdultPassengerCount(int numberOfAdultsToSelect) {
        int numberOfAdultsSelected = getSelectedAdultPassengerCount();
        while (numberOfAdultsToSelect < numberOfAdultsSelected && numberOfAdultsSelected != 1) {
            context.page().locator(DECREASE_ADULT_PASSENGER_COUNT).click();
            numberOfAdultsSelected -= 1;
        }
        while (numberOfAdultsToSelect > numberOfAdultsSelected) {
            context.page().locator(INCREASE_ADULT_PASSENGER_COUNT).click();
            numberOfAdultsSelected += 1;
        }
    }

    private int getSelectedAdultPassengerCount() {
        return Integer.parseInt(context.page().locator(ADULT_PASSENGER_COUNT).getAttribute("value"));
    }
}
