package com.znsio.teswiz.screen.calculator;


public abstract class CalculatorScreen {

    public static CalculatorScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(CalculatorScreen.class);
    }

    public abstract CalculatorScreen handlePopupIfPresent();

    public abstract CalculatorScreen selectNumber(String number);

    public abstract CalculatorScreen pressOperation(String operation);
}
