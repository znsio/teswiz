package com.znsio.teswiz.runner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.Stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.znsio.teswiz.runner.Setup.EXECUTED_ON;
import static com.znsio.teswiz.runner.Setup.PARALLEL;

class LocalDevicesSetup {
    private static final Logger LOGGER = LogManager.getLogger(LocalDevicesSetup.class.getName());
    private static final String APPIUM_SETTINGS = "io.appium.settings";
    private static final String UNINSTALL = "uninstall";
    private static final String GETPROP = "getprop";

    private LocalDevicesSetup() {
        LOGGER.debug("LocalDevicesSetup - private constructor");
    }

    static void setupLocalExecution() {
        int numberOfDevicesForParallelExecution = setupLocalDevices().size();
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

    private static List<JadbDevice> setupLocalDevices() {
        startADBServerForLocalDevice();
        JadbConnection jadb = new JadbConnection();
        List<JadbDevice> deviceList;
        try {
            deviceList = jadb.getDevices();
        } catch (IOException | JadbException e) {
            throw new EnvironmentSetupException("Unable to get devices information", e);
        }
        LOGGER.info("Number of Devices connected: " + deviceList.size());
        return deviceList;
    }

    private static void startADBServerForLocalDevice() {
        LOGGER.info("Start ADB server");
        String[] listOfDevices = new String[]{"adb", "devices"};
        CommandLineExecutor.execCommand(listOfDevices);
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

    private static int getBootedIOSSimulators()  {
        String[] xcrunCommand = {"xcrun simctl list devices | grep Booted"};
        return getListOfIOSDevices(xcrunCommand, false);
    }

    private static int getConnectedIOSDevices()  {
        String[] xcrunCommand = {"ios", "list"};
        return getListOfIOSDevices(xcrunCommand, true);
    }

    private static int getListOfIOSDevices(String[] xcrunCommand, boolean isReal) {
        int numberOfDevices = 0;
        CommandLineResponse commandLineResponse = CommandLineExecutor.execCommand(xcrunCommand);
        String commandOutput = commandLineResponse.getStdOut();
        if (isReal) {
            JsonObject asJsonObject = JsonParser.parseString(commandOutput).getAsJsonObject();
            JsonArray deviceList = asJsonObject.get("deviceList").getAsJsonArray();
            numberOfDevices = deviceList.size();
            LOGGER.info("Number of real devices: %d".formatted(numberOfDevices));
            LOGGER.debug(deviceList);
        } else  {
            numberOfDevices = commandOutput.split("\n").length;
            LOGGER.info("Number of simulators: %d".formatted(numberOfDevices));
        }
        return numberOfDevices;
    }

    static void setupLocalIOSExecution() {
        int numberOfRealDevicesForParallelExecution = getConnectedIOSDevices();
        int numberOfSimulatorsForParallelExecution = getBootedIOSSimulators();
        if ((numberOfSimulatorsForParallelExecution + numberOfRealDevicesForParallelExecution) == 0) {
            throw new EnvironmentSetupException("No devices available to run the tests");
        }
        Integer providedParallelCount = Setup.getIntegerValueFromConfigs(PARALLEL);
        if (numberOfSimulatorsForParallelExecution < providedParallelCount) {
            throw new EnvironmentSetupException(String.format(
                    "Fewer devices (%d) available to run the tests in parallel (Expected more " + "than: %d)",
                    numberOfSimulatorsForParallelExecution, providedParallelCount));
        }
        Setup.addIntegerValueToConfigs(PARALLEL, providedParallelCount);
        Setup.addToConfigs(EXECUTED_ON, "Local Devices");
    }
}

