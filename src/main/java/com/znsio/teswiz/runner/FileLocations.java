package com.znsio.teswiz.runner;

import java.io.File;

import static com.znsio.teswiz.tools.OverriddenVariable.getOverriddenStringValue;

public interface FileLocations {

    String OUTPUT_DIRECTORY =
            getOverriddenStringValue("OUTPUT_DIRECTORY") != null
            ? File.separator + getOverriddenStringValue("OUTPUT_DIRECTORY") + File.separator
            : File.separator + "target" + File.separator;

    String SERVER_CONFIG_JSON = OUTPUT_DIRECTORY + "server.json";
    String REPORTS_DIRECTORY = OUTPUT_DIRECTORY + "reports" + File.separator;

    String APPIUM_LOGS_DIRECTORY = OUTPUT_DIRECTORY + "appiumlogs" + File.separator;
    String DEVICE_LOGS_DIRECTORY = "deviceLogs" + File.separator;
    String SCREENSHOTS_DIRECTORY = "screenshots" + File.separator;
}
