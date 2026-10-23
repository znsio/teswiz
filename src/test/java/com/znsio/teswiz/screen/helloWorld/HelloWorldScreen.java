package com.znsio.teswiz.screen.helloWorld;


public abstract class HelloWorldScreen {

    public static HelloWorldScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(HelloWorldScreen.class);
    }

    public abstract HelloWorldScreen generateRandomNumber(int counter);
}
