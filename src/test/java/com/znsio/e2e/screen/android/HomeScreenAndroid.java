package com.znsio.e2e.screen.android;

import com.znsio.e2e.screen.HomeScreen;
import com.znsio.e2e.screen.LoginScreen;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;

public class HomeScreenAndroid extends HomeScreen {
    private final Driver driver;
    private final Visual visually;
    private final String screenName = HomeScreenAndroid.class.getSimpleName();

    private final String loginScreenAccessibilityId = "Login Screen";

    public HomeScreenAndroid (Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        visually.takeScreenshot(screenName, "Home screen");
    }

    @Override
    public LoginScreen selectLoginTest () {
        driver.findElementByAccessibilityId(loginScreenAccessibilityId).click();
        return LoginScreen.get();
    }
}
