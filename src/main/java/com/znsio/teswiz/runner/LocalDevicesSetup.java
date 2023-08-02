package com.znsio.teswiz.runner;

import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import org.apache.log4j.Logger;
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

import static com.znsio.teswiz.runner.Setup.*;

class LocalDevicesSetup {
    private static final Logger LOGGER = Logger.getLogger(LocalDevicesSetup.class.getName());
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

    private static List<String> getBootedIOSSimulators()  {
        List<String> bootedSimulators = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("xcrun simctl list devices");

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            Pattern pattern = Pattern.compile("(.*)Booted");
            Matcher matcher = pattern.matcher(output.toString());
            while (matcher.find()) {
                bootedSimulators.add(matcher.group(1));
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Failed to fetch iOS Simulators. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new EnvironmentSetupException("No simulators detected to run the tests");
        }

        return bootedSimulators;
    }

    static void setupLocalIOSExecution() {
        int numberOfDevicesForParallelExecution = getBootedIOSSimulators().size();
        if (numberOfDevicesForParallelExecution == 0) {
            throw new EnvironmentSetupException("No devices available to run the tests");
        }
        Integer providedParallelCount = Setup.getIntegerValueFromConfigs(PARALLEL);
        if (numberOfDevicesForParallelExecution < providedParallelCount) {
            throw new EnvironmentSetupException(String.format(
                    "Fewer devices (%d) available to run the tests in parallel (Expected more " + "than: %d)",
                    numberOfDevicesForParallelExecution, providedParallelCount));
        }
        Setup.addIntegerValueToConfigs(PARALLEL, providedParallelCount);
        Setup.addToConfigs(EXECUTED_ON, "Local Devices");
    }
}

