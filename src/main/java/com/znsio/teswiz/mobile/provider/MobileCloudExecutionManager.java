package com.znsio.teswiz.mobile.provider;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.exceptions.InvalidTestDataException;

public final class MobileCloudExecutionManager {
    private static final Logger LOGGER = LogManager.getLogger(MobileCloudExecutionManager.class.getName());
    private static final String CLOUD_NAME_NOT_SUPPORTED_MESSAGE = "Provided cloudName: '%s' is not supported";

    private final Consumer<String> browserStackSetup;
    private final Consumer<String> lambdaTestSetup;
    private final Consumer<String> headSpinSetup;
    private final Consumer<String> pCloudySetup;
    private final Runnable browserStackCleanup;

    public MobileCloudExecutionManager(Consumer<String> browserStackSetup,
            Consumer<String> lambdaTestSetup,
            Consumer<String> headSpinSetup,
            Consumer<String> pCloudySetup,
            Runnable browserStackCleanup) {
        this.browserStackSetup = browserStackSetup;
        this.lambdaTestSetup = lambdaTestSetup;
        this.headSpinSetup = headSpinSetup;
        this.pCloudySetup = pCloudySetup;
        this.browserStackCleanup = browserStackCleanup;
    }

    public void setupCloudExecution(String cloudName, String cloudApiUrl) {
        switch (normalizeCloudName(cloudName)) {
            case "headspin":
                headSpinSetup.accept(cloudApiUrl);
                break;
            case "pcloudy":
                pCloudySetup.accept(cloudApiUrl);
                break;
            case "browserstack":
                browserStackSetup.accept(cloudApiUrl);
                break;
            case "lambdatest":
                lambdaTestSetup.accept(cloudApiUrl);
                break;
            default:
                throw unsupportedCloud(cloudName);
        }
    }

    public void cleanupCloudExecution(String cloudName) {
        switch (normalizeCloudName(cloudName)) {
            case "browserstack":
                browserStackCleanup.run();
                break;
            case "headspin":
            case "pcloudy":
            case "lambdatest":
            case "saucelabs":
            case "docker":
                LOGGER.info(String.format("No cleanup required for cloud: '%s'", cloudName));
                break;
            default:
                throw unsupportedCloud(cloudName);
        }
    }

    private String normalizeCloudName(String cloudName) {
        return null == cloudName ? "" : cloudName.toLowerCase();
    }

    private InvalidTestDataException unsupportedCloud(String cloudName) {
        return new InvalidTestDataException(String.format(CLOUD_NAME_NOT_SUPPORTED_MESSAGE, cloudName));
    }
}
