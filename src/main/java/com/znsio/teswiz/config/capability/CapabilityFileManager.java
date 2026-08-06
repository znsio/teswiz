package com.znsio.teswiz.config.capability;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.JsonPrettyPrinter;

public final class CapabilityFileManager {
    private static final Logger LOGGER = LogManager.getLogger(CapabilityFileManager.class.getName());

    private CapabilityFileManager() {
    }

    public static String getPathForFileInLogDir(String fullFilePath, String logDir) {
        LOGGER.info(String.format("\tgetPathForFileInLogDir: fullFilePath: %s", fullFilePath));
        Path path = Paths.get(fullFilePath);
        String fileName = path.getFileName().toString();
        String newFileName = new File(logDir + File.separator + fileName).getAbsolutePath();
        LOGGER.info(String.format("\tNew file available here: %s", newFileName));
        return newFileName;
    }

    public static String saveDeviceFarmCapabilities(String capabilityFile,
            Map<String, Map> loadedCapabilityFile,
            List<?> listOfDevices,
            String logDir) {
        Map serverConfig = (Map) loadedCapabilityFile.get("serverConfig");
        Map server = (Map) serverConfig.get("server");
        Map plugin = (Map) server.get("plugin");
        Map deviceFarm = (Map) plugin.get("device-farm");
        Map cloudConfig = (Map) deviceFarm.get("cloud");
        cloudConfig.put("devices", listOfDevices);

        LOGGER.info(String.format("Updated Device Lab Capabilities file: %n%s",
                JsonPrettyPrinter.prettyPrint(loadedCapabilityFile)));

        String updatedCapabilitiesFile = getPathForFileInLogDir(capabilityFile, logDir);
        JsonFile.saveJsonToFile(loadedCapabilityFile, updatedCapabilitiesFile);
        return updatedCapabilitiesFile;
    }
}
