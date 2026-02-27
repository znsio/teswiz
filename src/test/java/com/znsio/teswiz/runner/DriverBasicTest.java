package com.znsio.teswiz.runner;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DriverBasicTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_lambdatest_web_config.properties";
    private static final String ANDROID_CONFIG_FILE =
            "./configs/theapp/theapp_lambdatest_android_config.properties";
    private static final String IOS_CONFIG_FILE =
            "./configs/theapp/theapp_lambdatest_ios_config.properties";

    @AfterEach
    void cleanUp() {
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void pdfDriverShouldExposeExpectedDefaults() {
        setupConfig();
        TestExecutionContext context = new TestExecutionContext("driver-pdf-test");

        Driver driver = new Driver("me", Platform.pdf, context, "sample.pdf");

        assertThat(driver.getType()).isEqualTo(Driver.PDF_DRIVER);
        assertThat(driver.getInnerDriver()).isNull();
        assertThat(driver.isDriverRunningInHeadlessMode()).isFalse();
        assertThat(driver.isMobilePlatform()).isFalse();
    }

    @Test
    void mobilePlatformFlagShouldFollowProvidedPlatformEvenForPdfDriverType() {
        setupConfig(ANDROID_CONFIG_FILE);
        TestExecutionContext androidContext = new TestExecutionContext("driver-android-mobile-flag-test");
        Driver androidPlatformDriver = new Driver("me", Platform.android, androidContext, "sample.pdf");
        assertThat(androidPlatformDriver.isMobilePlatform()).isTrue();

        SessionContext.remove(Thread.currentThread().getId());

        setupConfig(IOS_CONFIG_FILE);
        TestExecutionContext iosContext = new TestExecutionContext("driver-ios-mobile-flag-test");
        Driver iosPlatformDriver = new Driver("me", Platform.iOS, iosContext, "sample.pdf");
        assertThat(iosPlatformDriver.isMobilePlatform()).isTrue();

        SessionContext.remove(Thread.currentThread().getId());

        setupConfig(CONFIG_FILE);
        TestExecutionContext webContext = new TestExecutionContext("driver-web-mobile-flag-test");
        Driver windowsPlatformDriver = new Driver("me", Platform.windows, webContext, "sample.pdf");
        assertThat(windowsPlatformDriver.isMobilePlatform()).isFalse();
    }

    private static void setupConfig() {
        setupConfig(CONFIG_FILE);
    }

    private static void setupConfig(String configFile) {
        Setup.load(configFile);
        Setup.loadAndUpdateConfigParameters(configFile);
    }
}
