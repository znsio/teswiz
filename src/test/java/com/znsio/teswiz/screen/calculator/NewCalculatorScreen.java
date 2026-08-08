package com.znsio.teswiz.screen.calculator;


public abstract class NewCalculatorScreen {

    public static NewCalculatorScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(NewCalculatorScreen.class);
    }

    public abstract NewCalculatorScreen selectNumber(String number);

    public abstract NewCalculatorScreen pressOperation(String operation);

    public abstract NewCalculatorScreen launch();
}
