package com.znsio.teswiz.runner.atd;

import com.github.dockerjava.api.model.Device;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static com.znsio.teswiz.runner.atd.FileLocations.SERVER_CONFIG;
import static java.lang.System.getProperty;

public class ATDRunner {
    public static final String USER_DIR = "user.dir";
    private static final String ANDROID = "android";
    private static final String BOTH = "both";
    private static final String IOS = "iOS";
    private static final Logger LOGGER = LogManager.getLogger(ATDRunner.class.getName());
    private final CustomCapabilities capabilities;


    public ATDRunner() throws Exception {
        setLog4jCompatibility();
        capabilities = CustomCapabilities.getInstance();
        writeServiceConfig();
        ATD_AppiumServerManager appiumServerManager = new ATD_AppiumServerManager();
        appiumServerManager.startAppiumServer("127.0.0.1"); //Needs to be removed
        List<Device> devices = Devices.getConnectedDevices();
        createOutputDirectoryIfNotExist();
    }

    private void setLog4jCompatibility() {
        // Migrating from Log4j 1.x to 2.x - https://logging.apache.org/log4j/2.x/manual/migration.html
        System.setProperty("log4j1.compatibility", "true");
    }

    private void writeServiceConfig() {
        JSONObject serverConfig = CustomCapabilities.getInstance().getCapabilityObjectFromKey("serverConfig");
        try (FileWriter writer = new FileWriter(new File(getProperty("user.dir") + SERVER_CONFIG))) {
            writer.write(serverConfig.toString());
            writer.flush();
        } catch (IOException e) {
            ExceptionUtils.getStackTrace(e);
        }
    }

    private void createOutputDirectoryIfNotExist() {
        File file = new File(System.getProperty(USER_DIR), FileLocations.OUTPUT_DIRECTORY);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

}
