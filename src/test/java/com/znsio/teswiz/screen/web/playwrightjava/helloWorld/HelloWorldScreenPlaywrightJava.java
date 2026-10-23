package com.znsio.teswiz.screen.web.playwrightjava.helloWorld;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.helloWorld.HelloWorldScreen;

public class HelloWorldScreenPlaywrightJava extends HelloWorldScreen {
    private static final String ENGINE_NAME = "playwright-java";

    public HelloWorldScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public HelloWorldScreen generateRandomNumber(int counter) {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "HelloWorld is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
