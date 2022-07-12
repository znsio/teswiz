package com.znsio.sample.e2e;

import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import static com.znsio.e2e.runner.Setup.CLOUD_KEY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RunnerTest {
    private final String stepDefDir = "com/znsio/e2e/steps";
    private final String logDir;

    {
        DateTime now = DateTime.now();
        logDir = "./target/" + now.getDayOfMonth() + "-" + now.getMonthOfYear() + "-" + now.getYear() + "_" + now.getHourOfDay() + "-" + now.getMinuteOfHour();
        System.setProperty("LOG_DIR", logDir);
        System.setProperty("OUTPUT_DIRECTORY", logDir);
    }

    @Test
    void localDefault() {
        String featuresDir = "./src/test/resources";
        System.setProperty("RUN_IN_CI", "false");
        System.setProperty("TAG", "@theapp");
        Runner runner = new Runner("./src/test/resources/configs/calculator_config.properties", stepDefDir, featuresDir);
        String baseUrl = Runner.getFromEnvironmentConfiguration("BASE_URL");
        assertThat(baseUrl).as("environment config is incorrect")
                           .isEqualTo("http://the-internet.herokuapp.com/");

        String actualTestData = Runner.getTestData("GMAIL_USER_1_EMAIL");
        assertThat(actualTestData).as("environment config is incorrect")
                                  .isEqualTo("mytestemail@gmail.com");
    }

    @Test
    void localWindows() {
        String featuresDir = "./src/test/resources";
        System.setProperty("RUN_IN_CI", "false");
        System.setProperty("PLATFORM", "windows");
        System.setProperty("TAG", "@notepad");
        Runner runner = new Runner("./src/test/resources/configs/windows_config.properties", stepDefDir, featuresDir);
    }

    @Test
    void localAndroidCalculator() {
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty("PLATFORM", Platform.android.name());
        System.setProperty("TAG", "@calculator");
        // System.setProperty("APP_PACKAGE_NAME", "com.android2.calculator3");
        // System.setProperty("atd_android_appPackage", "com.android2.calculator3");
        // System.setProperty("APP_PATH", "./src/test/resources/sampleApps/AndroidCalculator.apk");
        Runner runner = new Runner("./src/test/resources/configs/calculator_config.properties", stepDefDir, featuresDir);
        // runner.printProcessedConfiguration();
    }

    @Test
    void localAndroidTheApp() {
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty("PLATFORM", Platform.android.name());
        System.setProperty("TAG", "@theapp");
        System.setProperty("CAPS", "src/test/resources/com/znsio/e2e/features/caps/theapp_capabilities.json");
        // System.setProperty("APP_PACKAGE_NAME", "com.android2.calculator3");
        // System.setProperty("atd_android_appPackage", "com.android2.calculator3");
        // System.setProperty("APP_PATH", "./src/test/resources/sampleApps/AndroidCalculator.apk");
        Runner runner = new Runner("./src/test/resources/configs/theapp_config.properties", stepDefDir, featuresDir);
        // runner.printProcessedConfiguration();
    }

    @Test
    void localAndroidMultiDevice() {
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty("TAG", "@multiuser-android and @multidevice");
        System.setProperty("PLATFORM", Platform.android.name());
        System.setProperty("atd_calculator_capabilities_android_appPackage", "com.android2.calculator3");
        System.setProperty("atd_calculator_capabilities_android_app_local", "./src/test/resources/sampleApps/AndroidCalculator.apk");
        Runner runner = new Runner("./src/test/resources/configs/calculator_config.properties", stepDefDir, featuresDir);
        // runner.printProcessedConfiguration();
    }

    @Test
    void pCloudyAndroidMultiDevice() {
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty("TAG", "@multiuser-android and @multidevice");
        System.setProperty("PLATFORM", Platform.android.name());
        Runner runner = new Runner("./src/test/resources/configs/pcloudy_config.properties", stepDefDir, featuresDir);
        // runner.printProcessedConfiguration();
    }

    @Test
    void localWeb() {
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty("PLATFORM", Platform.web.name());
        Runner runner = new Runner("./src/test/resources/configs/theapp_config.properties", stepDefDir, featuresDir);
        // runner.printProcessedConfiguration();
    }

    @Test
    void localMultiUserAndroidWebTest() {
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty("TAG", "@multiuser-android-web");
        Runner runner = new Runner("./src/test/resources/configs/calculator_config.properties", stepDefDir, featuresDir);
    }

    @Test
    void multiUserAndroidTest() {
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty("TAG", "@multiuser-android and @login");
        Runner runner = new Runner("./src/test/resources/configs/calculator_config.properties", stepDefDir, featuresDir);
    }

    @Test
    void headspinWeb() {
        String HEADSPIN_KEY = System.getenv("HEADSPIN_KEY");
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty("PLATFORM", Platform.web.name());
        System.setProperty("RUN_IN_CI", "true");
        System.setProperty("TAG", "@login");
        System.setProperty(CLOUD_KEY, HEADSPIN_KEY);
        Runner runner = new Runner("./src/test/resources/configs/headspin_config.properties", stepDefDir, featuresDir);
    }

    @Test
    void headspinMultiUserAndroidWebTest() {
        String HEADSPIN_KEY = System.getenv("HEADSPIN_KEY");
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty(CLOUD_KEY, HEADSPIN_KEY);
        System.setProperty("RUN_IN_CI", "true");
        System.setProperty("TAG", "@multiuser-android-web");
        Runner runner = new Runner("./src/test/resources/configs/headspin_config.properties", stepDefDir, featuresDir);
    }

    @Test
    void headspinAndroidTest() {
        String HEADSPIN_KEY = System.getenv("HEADSPIN_KEY");
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty("RUN_IN_CI", "true");
        System.setProperty("TAG", "@login");
        System.setProperty(CLOUD_KEY, HEADSPIN_KEY);
        Runner runner = new Runner("./src/test/resources/configs/headspin_config.properties", stepDefDir, featuresDir);
    }

    @Test
    void headspinMultiUserAndroidTest() {
        String HEADSPIN_KEY = System.getenv("HEADSPIN_KEY");
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty("RUN_IN_CI", "true");
        System.setProperty("TAG", "@multiuser-android and @login");
        System.setProperty(CLOUD_KEY, HEADSPIN_KEY);
        Runner runner = new Runner("./src/test/resources/configs/headspin_config.properties", stepDefDir, featuresDir);
    }

    @Test
    void pCloudyAndroidCloudTest() {
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty("RUN_IN_CI", "true");
        System.setProperty("TAG", "@login");
        Runner runner = new Runner("./src/test/resources/configs/pcloudy_config.properties", stepDefDir, featuresDir);
    }

    @Test
    void pCloudyMultiUserAndroidCloudTest() {
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty("RUN_IN_CI", "true");
        System.setProperty("TAG", "@multiuser-android and @login");
        Runner runner = new Runner("./src/test/resources/configs/pcloudy_config.properties", stepDefDir, featuresDir);
    }

    @Test
    void browserStackAndroidCloudTest() {
        String featuresDir = "./src/test/resources/com/znsio/e2e/features";
        System.setProperty("RUN_IN_CI", "true");
        System.setProperty("PLATFORM", Platform.android.name());
        System.setProperty("APP_PACKAGE_NAME", "com.appiumpro.the_app");
        System.setProperty("atd_android_appPackage", "com.appiumpro.the_app");
        System.setProperty("APP_PATH", "./src/test/resources/sampleApps/AndroidCalculator.apk");
        // System.setProperty("TAG", "@login");
        Runner runner = new Runner("./src/test/resources/configs/browserStack_config.properties", stepDefDir, featuresDir);
    }

}