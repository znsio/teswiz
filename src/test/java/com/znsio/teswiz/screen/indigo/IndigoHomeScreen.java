package com.znsio.teswiz.screen.indigo;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.indigo.IndigoHomeScreenAndroid;
import com.znsio.teswiz.screen.web.indigo.IndigoHomeScreenWeb;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class IndigoHomeScreen {
    private static final String SCREEN_NAME = IndigoHomeScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

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
