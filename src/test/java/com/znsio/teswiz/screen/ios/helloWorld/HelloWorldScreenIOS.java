package com.znsio.teswiz.screen.ios.helloWorld;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.ScreenShotScreenAndroid;
import com.znsio.teswiz.screen.helloWorld.HelloWorldScreen;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public class HelloWorldScreenIOS extends HelloWorldScreen {
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = ScreenShotScreenAndroid.class.getSimpleName();
    private final By byMakeRandomNumberCheckbox = AppiumBy.accessibilityId("MakeRandomNumberCheckbox");

    public HelloWorldScreenIOS(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public HelloWorldScreen generateRandomNumber(int counter) {
        visually.checkWindow(SCREEN_NAME, "MakeRandomNumberCheckbox-beforeClick-" + counter);
        driver.findElement(byMakeRandomNumberCheckbox)
                .click();
        visually.checkWindow(SCREEN_NAME, "MakeRandomNumberCheckbox-afterClick-" + counter);
        return this;
    }
}
