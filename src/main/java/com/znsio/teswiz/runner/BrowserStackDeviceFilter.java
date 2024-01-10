package com.znsio.teswiz.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.JsonPath;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.znsio.teswiz.runner.Setup.getCurlProxyCommand;

class BrowserStackDeviceFilter {
    private static final Logger LOGGER = LogManager.getLogger(BrowserStackDeviceFilter.class.getName());

    private BrowserStackDeviceFilter() {
        LOGGER.debug("BrowserStackDeviceFilter - private constructor");
    }

    static List<BrowserStackDevice> getFilteredDevices(String authenticationUser,
                                                       String authenticationKey,
                                                       Map<String, String> filters, String logDir) {
        // fetch the browser list from browserstack
        List<BrowserStackDevice> filteredDevices = null;
        String filteredDeviceFileName = logDir + File.separator +
                                        "filteredDevicesFromBrowserStack.yml";
        String allAvailableBrowsersAndDevicesFileName = new File(
                logDir + File.separator + "allAvailableBrowsersAndDevices.json").getAbsolutePath();
        try {

            String[] curlCommand = getCurlCommandForFetchingAllAvailableDevices(authenticationUser,
                                                                                authenticationKey,
                                                                                allAvailableBrowsersAndDevicesFileName);

            CommandLineExecutor.execCommand(curlCommand);

            String documentContext = JsonPath.parse(
                    new File(allAvailableBrowsersAndDevicesFileName)).jsonString();
            final ObjectMapper objectMapper = new ObjectMapper();
            BrowserStackDevice[] langs = objectMapper.readValue(documentContext,
                                                                BrowserStackDevice[].class);
            List<BrowserStackDevice> langList = new ArrayList(Arrays.asList(langs));

            filteredDevices = applyFilters(langList, filters);
            ObjectMapper om = new ObjectMapper(new YAMLFactory());
            om.writeValue(new File(filteredDeviceFileName), filteredDevices);
        } catch(IOException e) {
            throw new InvalidTestDataException(String.format(
                    "Unable to fetch / save list of available devices with %n\tfilter: '%s' " +
                    "%n\tto temp file: '%s'",
                    filters, filteredDeviceFileName));
        }
        return filteredDevices;
    }

    @NotNull
    private static String[] getCurlCommandForFetchingAllAvailableDevices(String authenticationUser,
                                                                         String authenticationKey,
                                                                         String allAvailableBrowsersAndDevicesFileName) {
        return new String[]{
                "curl --insecure " + getCurlProxyCommand() + " -u \"" + authenticationUser + ":" + authenticationKey + "\"",
                "\"https://api.browserstack.com/automate/browsers.json\"",
                "> " + allAvailableBrowsersAndDevicesFileName};
    }

    private static List<BrowserStackDevice> applyFilters(List<BrowserStackDevice> all_devices,
                                                         Map<String, String> filters) {
        for(Map.Entry<String, String> filter : filters.entrySet()) {
            all_devices = filterPlatform(all_devices, filter);
            all_devices = filterDevice(all_devices, filter);
            all_devices = filterOS(all_devices, filter);
            all_devices = filterOsVersion(all_devices, filter);
            all_devices = filterBrowser(all_devices, filter);
            all_devices = filterBrowserVersion(all_devices, filter);
        }
        return all_devices;
    }

    private static List<BrowserStackDevice> filterBrowserVersion(
            List<BrowserStackDevice> all_devices, Map.Entry<String, String> filter) {
        if(filter.getKey().equals("Browser_version")) {
            all_devices = all_devices.stream()
                                     .filter(browserStackDevice -> (browserStackDevice.getBrowserVersion() != null && browserStackDevice.getBrowserVersion()
                                                                                                                                        .split(" ")[0].equals(
                                             filter.getValue()))).collect(Collectors.toList());
        }
        return all_devices;
    }

    private static List<BrowserStackDevice> filterBrowser(List<BrowserStackDevice> all_devices,
                                                          Map.Entry<String, String> filter) {
        if(filter.getKey().equals("Browser")) {
            all_devices = all_devices.stream()
                                     .filter(browserStackDevice -> StringUtils.equalsIgnoreCase(
                                             browserStackDevice.getBrowser(), (filter.getValue())))
                                     .collect(Collectors.toList());
        }
        return all_devices;
    }

    private static List<BrowserStackDevice> filterOsVersion(List<BrowserStackDevice> all_devices,
                                                            Map.Entry<String, String> filter) {
        if(filter.getKey().equals("Os_version")) {
            all_devices = all_devices.stream()
                                     .filter(browserStackDevice -> StringUtils.containsIgnoreCase(
                                             browserStackDevice.getOs_version(),
                                             (filter.getValue()))).collect(Collectors.toList());
        }
        return all_devices;
    }

    private static List<BrowserStackDevice> filterOS(List<BrowserStackDevice> all_devices,
                                                     Map.Entry<String, String> filter) {
        if(filter.getKey().equals("Os")) {
            all_devices = all_devices.stream()
                                     .filter(browserStackDevice -> StringUtils.equalsIgnoreCase(
                                             browserStackDevice.getOs(), (filter.getValue())))
                                     .collect(Collectors.toList());
        }
        return all_devices;
    }

    private static List<BrowserStackDevice> filterDevice(List<BrowserStackDevice> all_devices,
                                                         Map.Entry<String, String> filter) {
        if(filter.getKey().equals("Device")) {
            all_devices = all_devices.stream()
                                     .filter(browserStackDevice -> StringUtils.containsIgnoreCase(
                                             browserStackDevice.getDevice(), filter.getValue()))
                                     .collect(Collectors.toList());
        }
        return all_devices;
    }

    private static List<BrowserStackDevice> filterPlatform(List<BrowserStackDevice> all_devices,
                                                           Map.Entry<String, String> filter) {
        if(filter.getKey().equals("Platform")) {
            if(filter.getValue().equals("mobile")) {
                all_devices = all_devices.stream()
                                         .filter(browserStackDevice -> browserStackDevice.isRealMobile())
                                         .collect(Collectors.toList());
            } else {
                all_devices = all_devices.stream()
                                         .filter(browserStackDevice -> !browserStackDevice.isRealMobile())
                                         .collect(Collectors.toList());
            }
        }
        return all_devices;
    }
}
