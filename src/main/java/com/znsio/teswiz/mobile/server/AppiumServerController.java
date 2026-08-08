package com.znsio.teswiz.mobile.server;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.runner.CustomCapabilities;
import com.znsio.teswiz.runner.FileLocations;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.tools.FileUtils;
import com.znsio.teswiz.tools.OsUtils;
import com.znsio.teswiz.tools.SensitiveDataMasker;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;

public class AppiumServerController {
    private static final Logger LOGGER = LogManager.getLogger(AppiumServerController.class.getName());
    private static final String APPIUM_SERVER_LOG_LEVEL_CAPABILITY = "appiumServerLogLevel";
    private static final String DEFAULT_APPIUM_SERVER_LOG_LEVEL = "info";
    private static AppiumDriverLocalService appiumDriverLocalService;
    private final Supplier<String> cloudUrlSupplier;

    public AppiumServerController(Supplier<String> cloudUrlSupplier) {
        this.cloudUrlSupplier = cloudUrlSupplier;
    }

    private static AppiumDriverLocalService getAppiumDriverLocalService() {
        return appiumDriverLocalService;
    }

    private static void setAppiumDriverLocalService(AppiumDriverLocalService localService) {
        appiumDriverLocalService = localService;
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
        }
        String cloudUrlFromCapabilities = cloudUrlSupplier.get();
        cloudUrlFromCapabilities = normalizeRemoteHubUrl(cloudUrlFromCapabilities);
        LOGGER.info("{} Using Cloud Appium Server at: {}", LOGGER.getName(),
                SensitiveDataMasker.mask(cloudUrlFromCapabilities));
        return cloudUrlFromCapabilities;
    }

    static String normalizeRemoteHubUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Remote hub URL cannot be null/empty");
        }

        String url = baseUrl.trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (url.endsWith("/wd/hub")) {
            return url;
        }
        if (url.endsWith("/wd")) {
            return url + "/hub";
        }
        return url + "/wd/hub";
    }

    public void startAppiumServer(String host) {
        LOGGER.info("{} Starting Appium Server on Localhost", LOGGER.getName());
        FileUtils.createDirectory(OsUtils.getUserDirectory() + FileLocations.APPIUM_LOGS_DIRECTORY + "appium_logs.txt");
        AppiumServiceBuilder builder;
        try {
            builder = getAppiumServerBuilder()
                    .withLogFile(new File(
                            System.getProperty("user.dir")
                                    + FileLocations.APPIUM_LOGS_DIRECTORY
                                    + "appium_logs.txt"))
                    .withIPAddress(host)
                    .withTimeout(Duration.ofSeconds(60))
                    .withArgument(() -> "--config", System.getProperty("user.dir") + FileLocations.SERVER_CONFIG_JSON)
                    .withArgument(GeneralServerFlag.RELAXED_SECURITY)
                    .withArgument(() -> "--log-level", getAppiumServerLogLevel())
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
        AppiumDriverLocalService localService = builder.build();
        localService.start();
        LOGGER.info("{} Appium Server Started at......{}", LOGGER.getName(), localService.getUrl());
        setAppiumDriverLocalService(localService);
    }

    private AppiumServiceBuilder getAppiumServerBuilder() {
        if (CustomCapabilities.getInstance().getCapabilities().has("appiumServerPath")) {
            Path path = FileSystems.getDefault().getPath(CustomCapabilities.getInstance()
                    .getCapabilities().get("appiumServerPath").toString());
            String serverPath = path.normalize().toAbsolutePath().toString();
            LOGGER.info("Picking UserSpecified Path for AppiumServiceBuilder");
            return new AppiumServiceBuilder().withAppiumJS(new File(serverPath));
        }
        LOGGER.info("Picking Default Path for AppiumServiceBuilder");
        return new AppiumServiceBuilder();
    }

    private String getBasePath() {
        LOGGER.info("Picking UserSpecified Base Path");
        return CustomCapabilities.getInstance().getCapabilities().get("basePath").toString();
    }

    private String getAppiumServerLogLevel() {
        String appiumServerLogLevel = getCapabilitySectionLogLevel("android");
        if (StringUtils.isBlank(appiumServerLogLevel)) {
            appiumServerLogLevel = getCapabilitySectionLogLevel("iOS");
        }
        if (StringUtils.isBlank(appiumServerLogLevel)) {
            appiumServerLogLevel = DEFAULT_APPIUM_SERVER_LOG_LEVEL;
        }
        LOGGER.info("Using Appium server log level: {}", appiumServerLogLevel);
        return appiumServerLogLevel;
    }

    private String getCapabilitySectionLogLevel(String sectionName) {
        JSONObject capabilities = CustomCapabilities.getInstance().getCapabilities();
        if (!capabilities.has(sectionName)) {
            return null;
        }
        Object sectionObject = capabilities.get(sectionName);
        if (!(sectionObject instanceof JSONObject)) {
            return null;
        }
        JSONObject platformCapabilities = (JSONObject) sectionObject;
        if (!platformCapabilities.has(APPIUM_SERVER_LOG_LEVEL_CAPABILITY)) {
            return null;
        }
        String configuredLogLevel = platformCapabilities.get(APPIUM_SERVER_LOG_LEVEL_CAPABILITY).toString();
        return StringUtils.isBlank(configuredLogLevel) ? null : configuredLogLevel.trim();
    }
}
