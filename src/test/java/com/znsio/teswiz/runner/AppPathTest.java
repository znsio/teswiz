package com.znsio.teswiz.runner;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppPathTest {
    private static final String DIRECTORY_PATH = System.getProperty("user.dir") + File.separator + "temp"
            + File.separator + "unitTests" + File.separator + "sampleApps";
    private static final String lambdaTestAppReference = "lt://APP123";
    private static final String browserStackAppReference = "bs://APP456";

    @Test
    void givenLambdaTestAppReference_WhenCheckingAppPath_ThenReturnReferenceWithoutLocalValidation() {
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(lambdaTestAppReference, DIRECTORY_PATH);
        assertEquals(lambdaTestAppReference, actualAppPath);
    }

    @Test
    void givenBrowserStackAppReferenceWhenCheckingAppPathThenReturnReferenceWithoutLocalValidation() {
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(browserStackAppReference, DIRECTORY_PATH);
        assertEquals(browserStackAppReference, actualAppPath);
    }
}
