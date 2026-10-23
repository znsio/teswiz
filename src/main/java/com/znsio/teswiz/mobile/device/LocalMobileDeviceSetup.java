package com.znsio.teswiz.mobile.device;

import static com.znsio.teswiz.runner.Setup.EXECUTED_ON;
import static com.znsio.teswiz.runner.Setup.PARALLEL;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.tools.JsonPrettyPrinter;
import com.znsio.teswiz.tools.SensitiveDataMasker;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;

import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.Stream;

public final class LocalMobileDeviceSetup {
    private static final Logger LOGGER = LogManager.getLogger(LocalMobileDeviceSetup.class.getName());

    private LocalMobileDeviceSetup() {
    }

    public static void setupLocalExecution() {
        int numberOfDevicesForParallelExecution = setupLocalDevices().size();
        Integer providedParallelCount = Setup.getIntegerValueFromConfigs(PARALLEL);
        validateAndroidParallelAvailability(numberOfDevicesForParallelExecution, providedParallelCount);
        Setup.addIntegerValueToConfigs(PARALLEL, providedParallelCount);
        Setup.addToConfigs(EXECUTED_ON, "Local Devices");
    }

    public static void setupLocalIOSExecution() {
        int numberOfRealDevicesForParallelExecution = getConnectedIOSDevices();
        int numberOfSimulatorsForParallelExecution = getBootedIOSSimulators();
        Integer providedParallelCount = Setup.getIntegerValueFromConfigs(PARALLEL);
        validateIosParallelAvailability(numberOfRealDevicesForParallelExecution, numberOfSimulatorsForParallelExecution,
                providedParallelCount);
        Setup.addIntegerValueToConfigs(PARALLEL, providedParallelCount);
        Setup.addToConfigs(EXECUTED_ON, "Local Devices");
    }

    static void validateAndroidParallelAvailability(int availableDevices, int requestedParallelCount) {
        if (availableDevices == 0) {
            throw new EnvironmentSetupException("No devices available to run the tests");
        }
        if (availableDevices < requestedParallelCount) {
            throw new EnvironmentSetupException(String.format(
                    "Fewer devices (%d) available to run the tests in parallel (Expected more than: %d)",
                    availableDevices, requestedParallelCount));
        }
    }

    static void validateIosParallelAvailability(int availableRealDevices, int bootedSimulators,
            int requestedParallelCount) {
        if ((availableRealDevices + bootedSimulators) == 0) {
            throw new EnvironmentSetupException("No devices available to run the tests");
        }
        if (bootedSimulators < requestedParallelCount) {
            throw new EnvironmentSetupException(String.format(
                    "Fewer devices (%d) available to run the tests in parallel (Expected more than: %d)",
                    bootedSimulators, requestedParallelCount));
        }
    }

    private static List<JadbDevice> setupLocalDevices() {
        startADBServerForLocalDevice();
        JadbConnection jadb = new JadbConnection();
        try {
            List<JadbDevice> deviceList = jadb.getDevices();
            LOGGER.info("Number of Devices connected: " + deviceList.size());
            return deviceList;
        } catch (IOException | JadbException e) {
            throw new EnvironmentSetupException("Unable to get devices information", e);
        }
    }

    private static void startADBServerForLocalDevice() {
        LOGGER.info("Start ADB server");
        String[] listOfDevices = new String[] { "adb", "devices" };
        CommandLineExecutor.execCommand(listOfDevices);
    }

    @NotNull
    static String getAdbCommandOutputFromLocalDevice(JadbDevice device, String command, String args)
            throws IOException, JadbException {
        InputStream inputStream = device.executeShell(command, args);
        LOGGER.info("\tadb command: '{}', args: '{}'",
                SensitiveDataMasker.mask(command), SensitiveDataMasker.mask(args));
        String adbCommandOutput = Stream.readAll(inputStream, StandardCharsets.UTF_8).replaceAll("\n$", "");
        LOGGER.info("\tOutput: {}", SensitiveDataMasker.mask(adbCommandOutput));
        return adbCommandOutput;
    }

    private static int getBootedIOSSimulators() {
        String[] xcrunCommand = { "xcrun simctl list devices | grep Booted" };
        return getListOfIOSDevices(xcrunCommand, false);
    }

    private static int getConnectedIOSDevices() {
        String[] xcrunCommand = { "ios", "list" };
        return getListOfIOSDevices(xcrunCommand, true);
    }

    private static int getListOfIOSDevices(String[] xcrunCommand, boolean isReal) {
        CommandLineResponse commandLineResponse = CommandLineExecutor.execCommand(xcrunCommand);
        String commandOutput = commandLineResponse.getStdOut();
        if (isReal) {
            JsonObject asJsonObject = JsonParser.parseString(commandOutput).getAsJsonObject();
            JsonArray deviceList = asJsonObject.get("deviceList").getAsJsonArray();
            int numberOfDevices = deviceList.size();
            LOGGER.info(String.format("Number of iOS real devices: %d", numberOfDevices));
            LOGGER.debug("Connected iOS devices: {}",
                    SensitiveDataMasker.mask(JsonPrettyPrinter.prettyPrint(deviceList)));
            return numberOfDevices;
        }
        int numberOfDevices = commandOutput.split("\n").length;
        LOGGER.info(String.format("Number of iOS simulators: %d", numberOfDevices));
        return numberOfDevices;
    }
}
