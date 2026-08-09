package com.znsio.teswiz.screen.indigo;


public abstract class IndigoHomeScreen {

    public static IndigoHomeScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(IndigoHomeScreen.class);
    }

    public abstract IndigoHomeScreen selectFrom(String from);

    public abstract IndigoHomeScreen selectTo(String destination);

    public abstract IndigoHomeScreen selectNumberOfAdultPassengers(int numberOfAdults);

    public abstract IndigoHomeScreen selectJourneyType(String journeyType);

    public abstract IndigoFlightSearchResultsScreen searchFlightOptions();

    public abstract IndigoGiftVouchersScreen selectGiftVouchers();
}
