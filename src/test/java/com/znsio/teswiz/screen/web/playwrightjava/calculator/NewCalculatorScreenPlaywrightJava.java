package com.znsio.teswiz.screen.web.playwrightjava.calculator;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.calculator.NewCalculatorScreen;

public class NewCalculatorScreenPlaywrightJava extends NewCalculatorScreen {
    private static final String ENGINE_NAME = "playwright-java";

    public NewCalculatorScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public NewCalculatorScreen selectNumber(String number) {
        throw unsupported();
    }

    @Override
    public NewCalculatorScreen pressOperation(String operation) {
        throw unsupported();
    }

    @Override
    public NewCalculatorScreen launch() {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "NewCalculator is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
