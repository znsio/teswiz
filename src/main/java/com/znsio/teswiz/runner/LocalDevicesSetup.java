package com.znsio.teswiz.runner;

import com.github.device.Device;
import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
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

import static com.znsio.teswiz.runner.Setup.*;

public class LocalDevicesSetup {
    private static final Logger LOGGER = Logger.getLogger(LocalDevicesSetup.class.getName());
    private static final String APPIUM_SETTINGS = "io.appium.settings";
    private static final String UNINSTALL = "uninstall";
    private static final String GETPROP = "getprop";

    private static List<Device> devices;

    private LocalDevicesSetup() {
        LOGGER.debug("LocalDevicesSetup - private constructor");
    }

    static void setupLocalExecution() {
        List<Device> devices = setupLocalDevices();
        int numberOfDevicesForParallelExecution = devices.size();
        if(numberOfDevicesForParallelExecution == 0) {
            throw new EnvironmentSetupException("No devices available to run the tests");
        }
        Integer providedParallelCount = Setup.getIntegerValueFromConfigs(PARALLEL);
        if(numberOfDevicesForParallelExecution < providedParallelCount) {
            throw new EnvironmentSetupException(String.format(
                    "Fewer devices (%d) available to run the tests in parallel (Expected more " + "than: %d)",
                    numberOfDevicesForParallelExecution, providedParallelCount));
        }
        Setup.addIntegerValueToConfigs(PARALLEL, providedParallelCount);
        Setup.addToConfigs(EXECUTED_ON, "Local Devices");
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
                device.setApiLevel(getAdbCommandOutputFromLocalDevice(jadbDevice, GETPROP,
                                                                      "ro.build.version.sdk"));
                device.setDeviceManufacturer(getAdbCommandOutputFromLocalDevice(jadbDevice, GETPROP,
                                                                                "ro.product" +
                                                                                ".brand"));
                device.setDeviceModel(getAdbCommandOutputFromLocalDevice(jadbDevice, GETPROP,
                                                                         "ro.product.model"));
                device.setOsVersion(getAdbCommandOutputFromLocalDevice(jadbDevice, GETPROP,
                                                                       "ro.build.version.release"));
                devices.add(device);
                uninstallAppFromLocalDevice(device, Setup.getFromConfigs(APP_PACKAGE_NAME));
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
                                                                UNINSTALL,
                                                                APPIUM_UI_AUTOMATOR2_SERVER};
        CommandLineExecutor.execCommand(uninstallAppiumAutomator2Server);
        String[] uninstallAppiumSettings = new String[]{"adb", "-s", device.getUdid(), UNINSTALL,
                                                        APPIUM_SETTINGS};
        CommandLineExecutor.execCommand(uninstallAppiumSettings);

        if(Setup.getBooleanValueFromConfigs(CLEANUP_DEVICE_BEFORE_STARTING_EXECUTION)) {
            String[] uninstallApp = new String[]{"adb", "-s", device.getUdid(), UNINSTALL,
                                                 appPackageName};
            CommandLineExecutor.execCommand(uninstallApp);
        } else {
            LOGGER.info(
                    "skipping uninstalling of apk as the flag " +
                    "CLEANUP_DEVICE_BEFORE_STARTING_EXECUTION = false");
        }
    }

}
