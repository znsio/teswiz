package com.znsio.e2e.screen;

import com.znsio.e2e.entities.*;
import com.znsio.e2e.runner.*;
import com.znsio.e2e.screen.android.*;
import com.znsio.e2e.tools.*;
import org.apache.commons.lang3.*;
import org.apache.log4j.*;

import static com.znsio.e2e.runner.Runner.*;

public abstract class CalculatorScreen {
    private static final String SCREEN_NAME = CalculatorScreen.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);

    public static CalculatorScreen get() {
        Driver driver = fetchDriver(Thread.currentThread().getId());
        Platform platform = Runner.fetchPlatform(Thread.currentThread().getId());
        LOGGER.info(SCREEN_NAME + ": Driver type: " + driver.getType() + ": Platform: " + platform);
        Visual visually = fetchEyes(Thread.currentThread().getId());

        switch (platform) {
            case android:
                return new CalculatorScreenAndroid(driver, visually);
        }
        throw new NotImplementedException(SCREEN_NAME + " is not implemented in " + Runner.platform);
    }

    public abstract CalculatorScreen handlePopupIfPresent();

    public abstract CalculatorScreen selectNumber(String number);

    public abstract CalculatorScreen pressOperation(String operation);
}
