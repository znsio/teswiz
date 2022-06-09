package com.znsio.e2e.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.JsonPath;
import com.znsio.e2e.exceptions.InvalidTestDataException;
import com.znsio.e2e.tools.cmd.CommandLineExecutor;
import com.znsio.e2e.tools.cmd.CommandLineResponse;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BrowserStackDeviceFilter {
    public static final Map<String, String> env = System.getenv();
    public static final String USERNAME = "anand_kTb8F3";
    public static final String PASSWORD = "s8obLFbJxE2FgznTSvxA";
    private static final Logger LOGGER = Logger.getLogger(BrowserStackDeviceFilter.class.getName());

    public static List<BrowserStackDevice> getFilteredDevices(String authenticationUser, String authenticationKey, Map<String, String> filters, String logDir) {
        // fetch the browser list from browserstack
        List<BrowserStackDevice> filteredDevices = null;
        String filteredDeviceFileName = logDir + File.separator + "filteredDevicesFromBrowserStack.yml";
        String allAvailableBrowsersAndDevicesFileName = new File(logDir + File.separator + "allAvailableBrowsersAndDevices.json").getAbsolutePath();
        try {

            String[] curlCommand = new String[]{"curl --insecure -u \"" + authenticationUser + ":" + authenticationKey + "\"",
                                                "\"https://api.browserstack.com/automate/browsers.json\"", "> " + allAvailableBrowsersAndDevicesFileName};
            CommandLineResponse listOfBrowsersAndDevicesAvailableInBrowserStack = CommandLineExecutor.execCommand(curlCommand);

            String documentContext = JsonPath.parse(new File(allAvailableBrowsersAndDevicesFileName))
                                             .jsonString();
            final ObjectMapper objectMapper = new ObjectMapper();
            BrowserStackDevice[] langs = objectMapper.readValue(documentContext,
                                                                BrowserStackDevice[].class);//            BrowserStackDevice[] langs = objectMapper.readValue(documentContext,
            // BrowserStackDevice[].class);
            List<BrowserStackDevice> langList = new ArrayList(Arrays.asList(langs));

            filteredDevices = applyFilters(langList, filters);
            ObjectMapper om = new ObjectMapper(new YAMLFactory());
            om.writeValue(new File(filteredDeviceFileName), filteredDevices);
        } catch(IOException e) {
            throw new InvalidTestDataException(
                    String.format("Unable to fetch / save list of available devices with %n\tfilter: '%s' %n\tto temp file: '%s'", filters, filteredDeviceFileName));
        }
        return filteredDevices;
    }

    private static List<BrowserStackDevice> applyFilters(List<BrowserStackDevice> all_devices, Map<String, String> filters) {
        for(Map.Entry<String, String> filter : filters.entrySet()) {
            if(filter.getKey()
                     .equals("Platform")) {
                if(filter.getValue()
                         .equals("mobile")) {
                    all_devices = all_devices.stream()
                                             .filter(browserStackDevice -> browserStackDevice.isRealMobile())
                                             .collect(Collectors.toList());
                } else {
                    all_devices = all_devices.stream()
                                             .filter(browserStackDevice -> !browserStackDevice.isRealMobile())
                                             .collect(Collectors.toList());
                }
            }
            if(filter.getKey()
                     .equals("Device")) {
                all_devices = all_devices.stream()
                                         .filter(browserStackDevice -> browserStackDevice.getDevice()
                                                                                         .contains(filter.getValue()))
                                         .collect(Collectors.toList());
            }
            if(filter.getKey()
                     .equals("Os")) {
                all_devices = all_devices.stream()
                                         .filter(browserStackDevice -> browserStackDevice.getOs()
                                                                                         .equals(filter.getValue()))
                                         .collect(Collectors.toList());
            }
            if(filter.getKey()
                     .equals("Os_version")) {
                all_devices = all_devices.stream()
                                         .filter(browserStackDevice -> browserStackDevice.getOs_version()
                                                                                         .contains(filter.getValue()))
                                         .collect(Collectors.toList());
            }
            if(filter.getKey()
                     .equals("Browser")) {
                all_devices = all_devices.stream()
                                         .filter(browserStackDevice -> browserStackDevice.getBrowser()
                                                                                         .equals(filter.getValue()))
                                         .collect(Collectors.toList());
            }
            if(filter.getKey()
                     .equals("Browser_version")) {
                all_devices = all_devices.stream()
                                         .filter(browserStackDevice -> Double.parseDouble(browserStackDevice.getBrowserVersion()
                                                                                                            .split(" ")[0]) >= Double.parseDouble(filter.getValue()))
                                         .collect(Collectors.toList());
            }
        }
        return all_devices;
    }
}
