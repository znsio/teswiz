package com.znsio.e2e.screen.android;

import com.znsio.e2e.screen.*;
import com.znsio.e2e.tools.*;
import org.openqa.selenium.*;

public class HomeScreenAndroid extends HomeScreen {
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = HomeScreenAndroid.class.getSimpleName();

    private final String loginScreenAccessibilityId = "Login Screen";
    private final By byGoBackToHomeScreenButtonXpath = By.xpath("/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.RelativeLayout/android.widget.RelativeLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.view.ViewGroup/android.widget.ImageButton");
    private final By byEchoMessageXpath = By.xpath("//android.view.ViewGroup[@content-desc=\"Echo Box\"]/android.widget.TextView[1]");

    public HomeScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        visually.takeScreenshot(SCREEN_NAME, "Home screen");
    }

    @Override
    public LoginScreen selectLogin() {
        driver.findElementByAccessibilityId(loginScreenAccessibilityId).click();
        return LoginScreen.get();
    }

    @Override
    public HomeScreen goBack() {
        driver.waitForClickabilityOf(byGoBackToHomeScreenButtonXpath).click();
        return this;
    }

    @Override
    public EchoScreen selectEcho() {
        driver.waitForClickabilityOf(byEchoMessageXpath).click();
        return EchoScreen.get();
    }
}
