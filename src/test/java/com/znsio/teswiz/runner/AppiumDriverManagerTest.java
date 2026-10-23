package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.appmanagement.ApplicationState;

class AppiumDriverManagerTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_local_android_config.properties";

    @BeforeAll
    static void loadConfiguration() {
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
    }

    @Test
    void shouldQueryAndroidAppStateAfterTerminateBeforeStoppingDriver() {
        AndroidDriver androidDriver = mock(AndroidDriver.class);
        when(androidDriver.queryAppState("com.appiumpro.the_app"))
                .thenReturn(ApplicationState.NOT_RUNNING);

        ApplicationState applicationState = AppiumDriverManager.terminateAndroidApp(androidDriver,
                "com.appiumpro.the_app");

        assertThat(applicationState).isEqualTo(ApplicationState.NOT_RUNNING);

        InOrder inOrder = inOrder(androidDriver);
        inOrder.verify(androidDriver).terminateApp("com.appiumpro.the_app");
        inOrder.verify(androidDriver).queryAppState("com.appiumpro.the_app");
    }
}
