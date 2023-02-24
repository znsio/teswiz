package com.znsio.e2e.runner;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.testng.AssertJUnit.assertNotNull;

public class BrowserStackImageInjectionTest {
    AndroidDriver<AndroidElement> driver;

    @BeforeMethod
    public void getSetup() {
        String userName = "poojamanna_apMVFD";
        String accessKey = "1hYzxAksrMizydSLA2aP";
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("device", "Samsung Galaxy S20");
        caps.setCapability("os_version", "10.0");
        caps.setCapability("app", "bs://2bcd09991ea9fdfbf2875aa44d18dd685198e8d1");
        caps.setCapability("browserstack.enableCameraImageInjection", "true");

        try {
            driver = new AndroidDriver(new URL("https://" + userName + ":" + accessKey + "@hub-cloud.browserstack.com/wd/hub"), caps);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void toVerifyUploadURLIsGettingGenerated() {
        String mediaUrl = BrowserStackImageInjection.uploadToCloud("/Users/pooja.manna/IdeaProjects/teswiz_new/src/test/resources/sampleApps/sample-red-square-grunge-stamp-260nw-338250266.png");
        BrowserStackImageInjection.injectMediaToDriver(mediaUrl, driver);
        assertNotNull(mediaUrl);
    }

}