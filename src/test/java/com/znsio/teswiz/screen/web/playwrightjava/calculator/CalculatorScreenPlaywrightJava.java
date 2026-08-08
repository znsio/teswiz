package com.znsio.teswiz.screen.web.playwrightjava.calculator;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.calculator.CalculatorScreen;

public class CalculatorScreenPlaywrightJava extends CalculatorScreen {
    private static final String ENGINE_NAME = "playwright-java";

    public CalculatorScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public CalculatorScreen handlePopupIfPresent() {
        throw unsupported();
    }

    @Override
    public CalculatorScreen selectNumber(String number) {
        throw unsupported();
    }

    @Override
    public CalculatorScreen pressOperation(String operation) {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "Calculator is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
