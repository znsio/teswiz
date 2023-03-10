package com.znsio.teswiz.runner;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.AssertJUnit;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

class BrowserStackImageInjectionTest {
    private static final String LOG_DIR = "./target/testLogs";

    private static final Logger LOGGER = Logger.getLogger(
            BrowserStackImageInjectionTest.class.getName());

    static AndroidDriver<AndroidElement> driver;
    static String authenticationUser = null;
    static String authenticationKey = null;
    static String cloudName = null;

    @BeforeAll
    public static void getSetup() {
        System.setProperty("LOG_DIR", LOG_DIR);
        new File(LOG_DIR).mkdirs();

        authenticationUser = System.getenv("CLOUD_USER");
        authenticationKey = System.getenv("CLOUD_KEY");
        cloudName = System.getenv("CLOUD_NAME");
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("device", "Samsung Galaxy S20");
        caps.setCapability("os_version", "10.0");
        caps.setCapability("app", "bs://db95520c7e93e980a6fc6221554b1f905195cb66");
        caps.setCapability("browserstack.enableCameraImageInjection", "true");
        try {
            driver = new AndroidDriver(
                    new URL("https://" + authenticationUser + ":" + authenticationKey + "@hub" +
                            "-cloud.browserstack.com/wd/hub"),
                    caps);

        } catch(MalformedURLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

//    @Test
    public void toVerifyUploadURLIsGettingGenerated() {
        String imageFile = System.getProperty(
                "user.dir") + "/src/test/resources/images/handbag.jpg";
        AssertJUnit.assertNotNull(BrowserStackImageInjection.injectMediaToDriver(imageFile, driver,
                                                                                 authenticationUser,
                                                                                 authenticationKey));
    }

}