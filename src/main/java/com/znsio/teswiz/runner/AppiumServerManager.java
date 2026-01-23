package com.znsio.teswiz.runner;

import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.tools.FileUtils;
import com.znsio.teswiz.tools.OsUtils;
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

public class AppiumServerManager {

    private static final Logger LOGGER = LogManager.getLogger(AppiumServerManager.class.getName());
    private static AppiumDriverLocalService appiumDriverLocalService;

    private static AppiumDriverLocalService getAppiumDriverLocalService() {
        return appiumDriverLocalService;
    }

    private static void setAppiumDriverLocalService(AppiumDriverLocalService appiumDriverLocalService) {
        AppiumServerManager.appiumDriverLocalService = appiumDriverLocalService;
    }

    private URL getAppiumUrl() {
        return getAppiumDriverLocalService().getUrl();
    }

    public static void destroyAppiumNode() {
        if (null != getAppiumDriverLocalService()) {
            LOGGER.info("Shutting down Appium Server");
            getAppiumDriverLocalService().stop();
            if (getAppiumDriverLocalService().isRunning()) {
                LOGGER.info("AppiumServer didn't shut... Trying to quit again....");
                getAppiumDriverLocalService().stop();
            }
        }
    }

    public String getRemoteWDHubIP() {
        if (Runner.getCloudName().equalsIgnoreCase(Runner.NOT_SET)) {
            String appiumServerURL = getAppiumUrl().toString();
            LOGGER.info("{} Appium Server is running at: {}", LOGGER.getName(), appiumServerURL);
            return appiumServerURL;
        } else {
            String cloudUrlFromCapabilities = DeviceSetup.getCloudUrlFromCapabilities();
            cloudUrlFromCapabilities = ensureWdHub(cloudUrlFromCapabilities);
            LOGGER.info("{} Using Cloud Appium Server at: {}", LOGGER.getName(), cloudUrlFromCapabilities);
            return cloudUrlFromCapabilities;
        }
    }

    private static String ensureWdHub(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Remote hub URL cannot be null/empty");
        }

        String url = baseUrl.trim();

        // remove trailing slashes
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        // already correct
        if (url.endsWith("/wd/hub")) {
            return url;
        }

        // partially correct
        if (url.endsWith("/wd")) {
            return url + "/hub";
        }

        // plain base host
        return url + "/wd/hub";
    }

    public void startAppiumServer(String host) {
        LOGGER.info("{} Starting Appium Server on Localhost", LOGGER.getName());
        FileUtils.createDirectory(OsUtils.getUserDirectory() + FileLocations.APPIUM_LOGS_DIRECTORY + "appium_logs.txt");
        AppiumDriverLocalService appiumDriverLocalService;
        AppiumServiceBuilder builder = null;
        try {
            builder = getAppiumServerBuilder(host)
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
        } catch (Exception e) {
            throw new EnvironmentSetupException("Unable to start Appium Server", e);
        }
        if (CustomCapabilities.getInstance().getCapabilities().has("basePath")) {
            if (!StringUtils.isBlank(getBasePath())) {
                builder.withArgument(GeneralServerFlag.BASEPATH, getBasePath());
            }
        } else {
            builder.withArgument(GeneralServerFlag.BASEPATH, "/wd/hub");
        }
        appiumDriverLocalService = builder.build();
        appiumDriverLocalService.start();
        LOGGER.info("{} Appium Server Started at......{}", LOGGER.getName(), appiumDriverLocalService.getUrl());
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
