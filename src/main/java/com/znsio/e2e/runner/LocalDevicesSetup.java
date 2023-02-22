package com.znsio.e2e.runner;

import com.github.device.Device;
import com.znsio.e2e.exceptions.EnvironmentSetupException;
import com.znsio.e2e.tools.cmd.CommandLineExecutor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.znsio.e2e.runner.Runner.*;
import static com.znsio.e2e.runner.Setup.*;

public class LocalDevicesSetup {
    private static final Logger LOGGER = Logger.getLogger(LocalDevicesSetup.class.getName());

    private static List<Device> devices;

    static void setupLocalExecution() {
        List<Device> devices = setupLocalDevices();
        int numberOfDevicesForParallelExecution = devices.size();
        if(numberOfDevicesForParallelExecution == 0) {
            throw new EnvironmentSetupException("No devices available to run the tests");
        }
        Integer providedParallelCount = configsInteger.get(PARALLEL);
        if(numberOfDevicesForParallelExecution < providedParallelCount) {
            throw new EnvironmentSetupException(String.format(
                    "Fewer devices (%d) available to run the tests in parallel (Expected more " +
                    "than: %d)",
                    numberOfDevicesForParallelExecution, providedParallelCount));
        }
        configsInteger.put(PARALLEL, providedParallelCount);
        configs.put(EXECUTED_ON, "Local Devices");
    }

    private static List<Device> setupLocalDevices() {
        startADBServerForLocalDevice();
        if(null == devices) {
            JadbConnection jadb = new JadbConnection();
            List<JadbDevice> deviceList;
            devices = new ArrayList<>();
            try {
                deviceList = jadb.getDevices();
            } catch(IOException | JadbException e) {
                throw new EnvironmentSetupException("Unable to get devices information", e);
            }

            extractInfoFromEachLocalDevice(deviceList);

            LOGGER.info("Number of Devices connected: " + devices.size());
        }
        return devices;
    }

    private static void startADBServerForLocalDevice() {
        LOGGER.info("Start ADB server");
        String[] listOfDevices = new String[]{"adb", "devices"};
        CommandLineExecutor.execCommand(listOfDevices);
    }

    private static void extractInfoFromEachLocalDevice(List<JadbDevice> deviceList) {
        deviceList.forEach(jadbDevice -> {
            try {
                Device device = new Device();
                device.setName(jadbDevice.getSerial());
                device.setUdid(jadbDevice.getSerial());
                // device.setUdid(getAdbCommandOutput(jadbDevice, "getprop", "ro.serialno"));
                device.setApiLevel(getAdbCommandOutputFromLocalDevice(jadbDevice, "getprop",
                                                                      "ro.build.version.sdk"));
                device.setDeviceManufacturer(
                        getAdbCommandOutputFromLocalDevice(jadbDevice, "getprop",
                                                           "ro.product.brand"));
                device.setDeviceModel(getAdbCommandOutputFromLocalDevice(jadbDevice, "getprop",
                                                                         "ro.product.model"));
                device.setOsVersion(getAdbCommandOutputFromLocalDevice(jadbDevice, "getprop",
                                                                       "ro.build.version.release"));
                devices.add(device);
                uninstallAppFromLocalDevice(device, configs.get(APP_PACKAGE_NAME));
            } catch(IOException | JadbException e) {
                throw new EnvironmentSetupException("Unable to get devices information", e);
            }
        });
    }

    @NotNull
    private static String getAdbCommandOutputFromLocalDevice(JadbDevice device, String command,
                                                             String args) throws IOException,
                                                                                 JadbException {
        InputStream inputStream = device.executeShell(command, args);
        LOGGER.info("\tadb command: '" + command + "', args: '" + args + "', ");
        String adbCommandOutput = Stream.readAll(inputStream, StandardCharsets.UTF_8)
                                        .replaceAll("\n$", "");
        LOGGER.info("\tOutput: " + adbCommandOutput);
        return adbCommandOutput;
    }

    private static void uninstallAppFromLocalDevice(Device device, String appPackageName) {
        String[] uninstallAppiumAutomator2Server = new String[]{"adb", "-s", device.getUdid(),
                                                                "uninstall",
                                                                APPIUM_UI_AUTOMATOR2_SERVER};
        CommandLineExecutor.execCommand(uninstallAppiumAutomator2Server);
        String[] uninstallAppiumSettings = new String[]{"adb", "-s", device.getUdid(), "uninstall",
                                                        APPIUM_SETTINGS};
        CommandLineExecutor.execCommand(uninstallAppiumSettings);

        if(configsBoolean.get(CLEANUP_DEVICE_BEFORE_STARTING_EXECUTION)) {
            String[] uninstallApp = new String[]{"adb", "-s", device.getUdid(), "uninstall",
                                                 appPackageName};
            CommandLineExecutor.execCommand(uninstallApp);
        } else {
            LOGGER.info(
                    "skipping uninstalling of apk as the flag CLEANUP_DEVICE_BEFORE_STARTING_EXECUTION = false");
        }
    }

}
