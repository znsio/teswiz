package com.znsio.teswiz.screen.calculator;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.calculator.NewCalculatorScreenAndroid;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class NewCalculatorScreen {
    private static final String SCREEN_NAME = NewCalculatorScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static NewCalculatorScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(NewCalculatorScreen.class);
    }

    public abstract NewCalculatorScreen selectNumber(String number);

    public abstract NewCalculatorScreen pressOperation(String operation);

    public abstract NewCalculatorScreen launch();
}
