package com.znsio.teswiz.runner.atd;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.Duration;

public class ATD_AppiumServerManager {

    private static final Logger LOGGER = LogManager.getLogger(ATD_AppiumServerManager.class.getName());
    private static AppiumDriverLocalService appiumDriverLocalService;

    private static AppiumDriverLocalService getAppiumDriverLocalService() {
        return appiumDriverLocalService;
    }

    private static void setAppiumDriverLocalService(
            AppiumDriverLocalService appiumDriverLocalService) {
        ATD_AppiumServerManager.appiumDriverLocalService = appiumDriverLocalService;
    }

    private URL getAppiumUrl() {
        return getAppiumDriverLocalService().getUrl();
    }

    public void destroyAppiumNode() {
        LOGGER.info("Shutting down Appium Server");
        getAppiumDriverLocalService().stop();
        if (getAppiumDriverLocalService().isRunning()) {
            LOGGER.info("AppiumServer didn't shut... Trying to quit again....");
            getAppiumDriverLocalService().stop();
        }
    }

    public String getRemoteWDHubIP() {
        return getAppiumUrl().toString();
    }

    public void startAppiumServer(String host) throws Exception {
        LOGGER.info("{}Starting Appium Server on Localhost", LOGGER.getName());
        new File(
                System.getProperty("user.dir")
                + FileLocations.APPIUM_LOGS_DIRECTORY
                + "appium_logs.txt").getParentFile().mkdirs();
        AppiumDriverLocalService appiumDriverLocalService;
        AppiumServiceBuilder builder =
                getAppiumServerBuilder(host)
                        .withLogFile(new File(
                                System.getProperty("user.dir")
                                + FileLocations.APPIUM_LOGS_DIRECTORY
                                + "appium_logs.txt"))
                        .withIPAddress(host)
                        .withTimeout(Duration.ofSeconds(60))
                        .withArgument(() -> "--config", System.getProperty("user.dir") + FileLocations.SERVER_CONFIG_JSON)
                        .withArgument(GeneralServerFlag.RELAXED_SECURITY)
                        .withArgument(() -> "--log-level", "debug")
                        .usingAnyFreePort();
        if (CustomCapabilities.getInstance().getCapabilities().has("basePath")) {
            if (!StringUtils.isBlank(getBasePath())) {
                builder.withArgument(GeneralServerFlag.BASEPATH, getBasePath());
            }
        } else {
            builder.withArgument(GeneralServerFlag.BASEPATH, "/wd/hub");
        }
        appiumDriverLocalService = builder.build();
        appiumDriverLocalService.start();
        LOGGER.info("{}Appium Server Started at......{}", LOGGER.getName(), appiumDriverLocalService.getUrl());
        setAppiumDriverLocalService(appiumDriverLocalService);
    }

    private AppiumServiceBuilder getAppiumServerBuilder(String host) throws Exception {
        if (CustomCapabilities.getInstance().getCapabilities().has("appiumServerPath")) {
            Path path = FileSystems.getDefault().getPath(CustomCapabilities.getInstance()
                                                                 .getCapabilities().get("appiumServerPath").toString());
            String serverPath = path.normalize().toAbsolutePath().toString();
            LOGGER.info("Picking UserSpecified Path for AppiumServiceBuilder");
            return getAppiumServiceBuilderWithUserAppiumPath(serverPath);
        } else {
            LOGGER.info("Picking Default Path for AppiumServiceBuilder");
            return getAppiumServiceBuilderWithDefaultPath();
        }
    }

    private AppiumServiceBuilder getAppiumServiceBuilderWithUserAppiumPath(String appiumServerPath) {
        return new AppiumServiceBuilder().withAppiumJS(new File(appiumServerPath));
    }

    private AppiumServiceBuilder getAppiumServiceBuilderWithDefaultPath() {
        return new AppiumServiceBuilder();
    }

    private String getBasePath() {
        LOGGER.info("Picking UserSpecified Base Path");
        return CustomCapabilities.getInstance()
                .getCapabilities().get("basePath").toString();
    }

}
