package com.znsio.teswiz.config.capability;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CapabilityFileManagerTest {
    @Test
    void shouldBuildCapabilityFilePathInLogDir() {
        String updatedPath = CapabilityFileManager.getPathForFileInLogDir(
                "/tmp/caps/theapp_android_caps.json",
                "/tmp/reports");

        assertThat(updatedPath).isEqualTo(new java.io.File("/tmp/reports/theapp_android_caps.json").getAbsolutePath());
    }

    @Test
    void shouldSaveUpdatedDeviceFarmCapabilitiesToLogDir() throws Exception {
        Path logDir = Files.createTempDirectory("capability-log-dir");
        Path capabilityFile = Files.createTempFile("teswiz-caps", ".json");
        Map<String, Map> loadedCapabilities = sampleLoadedCapabilities();
        List<Map<String, String>> devices = new ArrayList<>();
        Map<String, String> device = new LinkedHashMap<>();
        device.put("platform", "android");
        device.put("deviceName", "Pixel 8");
        devices.add(device);

        String updatedFilePath = CapabilityFileManager.saveDeviceFarmCapabilities(
                capabilityFile.toString(),
                loadedCapabilities,
                devices,
                logDir.toString());

        assertThat(updatedFilePath).isEqualTo(logDir.resolve(capabilityFile.getFileName()).toFile().getAbsolutePath());
        assertThat(Files.exists(Path.of(updatedFilePath))).isTrue();
        String savedContent = Files.readString(Path.of(updatedFilePath));
        assertThat(savedContent).contains("\"deviceName\":\"Pixel 8\"");
    }

    private Map<String, Map> sampleLoadedCapabilities() {
        Map<String, Map> loadedCapabilities = new LinkedHashMap<>();
        loadedCapabilities.put("android", new LinkedHashMap<>());

        Map<String, Object> cloud = new LinkedHashMap<>();
        cloud.put("cloudName", "browserstack");
        cloud.put("devices", new ArrayList<>());

        Map<String, Object> deviceFarm = new LinkedHashMap<>();
        deviceFarm.put("cloud", cloud);
        Map<String, Object> plugin = new LinkedHashMap<>();
        plugin.put("device-farm", deviceFarm);
        Map<String, Object> server = new LinkedHashMap<>();
        server.put("plugin", plugin);
        Map<String, Object> serverConfig = new LinkedHashMap<>();
        serverConfig.put("server", server);
        loadedCapabilities.put("serverConfig", serverConfig);
        return loadedCapabilities;
    }
}
