package com.znsio.e2e.runner;

import com.znsio.teswiz.runner.BrowserStackImageInjection;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import org.apache.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.net.MalformedURLException;
import java.net.URL;
import static org.testng.AssertJUnit.assertNotNull;




public class BrowserStackImageInjectionTest  {
    private static final Logger LOGGER = Logger.getLogger(BrowserStackImageInjectionTest.class.getName());

     AndroidDriver<AndroidElement> driver;
     String authenticationUser = null;
     String authenticationKey = null;
    String cloudName = null;

    @BeforeMethod
    public void getSetup() {
        authenticationUser = System.getenv("CLOUD_USER");
        authenticationKey = System.getenv("CLOUD_KEY");
        cloudName = System.getenv("CLOUD_NAME");
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("device", "Samsung Galaxy S20");
        caps.setCapability("os_version", "10.0");
        caps.setCapability("app", "bs://db95520c7e93e980a6fc6221554b1f905195cb66");
        caps.setCapability("browserstack.enableCameraImageInjection", "true");
        try {
            driver = new AndroidDriver(new URL("https://" + authenticationUser + ":" + authenticationKey + "@hub-cloud.browserstack.com/wd/hub"), caps);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void toVerifyUploadURLIsGettingGenerated() {
        String imageFile = System.getProperty("user.dir")+ "/src/test/resources/images/handbag.jpg";
        assertNotNull(BrowserStackImageInjection.injectMediaToDriver(imageFile,driver,authenticationUser,authenticationKey));
    }

}