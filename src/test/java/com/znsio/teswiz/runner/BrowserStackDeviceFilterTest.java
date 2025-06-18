package com.znsio.teswiz.runner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.znsio.teswiz.tools.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BrowserStackDeviceFilterTest {

    List<BrowserStackDevice> browserStackDevices;
    private static final String LOG_DIR = "./target/testLogs";

    Method applyFilters;

    @BeforeAll
    public static void setupBefore() {
        System.setProperty("LOG_DIR", LOG_DIR);
        FileUtils.createDirectory(LOG_DIR);
    }

    @BeforeEach
    public void Setup() throws FileNotFoundException, NoSuchMethodException {
        String file = new File("").getAbsoluteFile()
                                  .getAbsolutePath() + "/src/test/resources/devicesList.json";
        JsonReader reader = new JsonReader(new FileReader(file));
        browserStackDevices = new Gson().fromJson(reader,
                                                  new TypeToken<List<BrowserStackDevice>>() {}.getType());

        applyFilters = BrowserStackDeviceFilter.class.getDeclaredMethod("applyFilters", List.class,
                                                                        Map.class);
        applyFilters.setAccessible(true);
    }

    @Test
    void userCanGetDevicesAfterApplyingOSBasedFilter() throws InvocationTargetException,
                                                              IllegalAccessException {
        Map<String, String> osBasedFilter = new HashMap<>();
        osBasedFilter.put("Os", "android");

        List<BrowserStackDevice> osBasedDevices = (List<BrowserStackDevice>) applyFilters.invoke(
                null, browserStackDevices, osBasedFilter);
        assertEquals(118, osBasedDevices.size());

        osBasedFilter.replace("Os", "Android");
        osBasedDevices = (List<BrowserStackDevice>) applyFilters.invoke(null, browserStackDevices,
                                                                        osBasedFilter);
        assertEquals(118, osBasedDevices.size());
    }

    @Test
    void userCanGetDevicesAfterApplyingOSVersionFilter() throws InvocationTargetException,
                                                                IllegalAccessException {
        Map<String, String> osVersionFilter = new HashMap<>();
        osVersionFilter.put("Os_version", "11.0");
        List<BrowserStackDevice> osVersionBasedDevices =
                (List<BrowserStackDevice>) applyFilters.invoke(
                null, browserStackDevices, osVersionFilter);

        assertEquals(23, osVersionBasedDevices.size());

        osVersionFilter.replace("Os_version", "Big Sur");
        osVersionBasedDevices = (List<BrowserStackDevice>) applyFilters.invoke(null,
                                                                               browserStackDevices,
                                                                               osVersionFilter);
        assertEquals(199, osVersionBasedDevices.size());

        osVersionFilter.replace("Os_version", "big Sur");
        osVersionBasedDevices = (List<BrowserStackDevice>) applyFilters.invoke(null,
                                                                               browserStackDevices,
                                                                               osVersionFilter);
        assertEquals(199, osVersionBasedDevices.size());
    }

    @Test
    void userCanGetDevicesAfterApplyingBrowserFilter() throws InvocationTargetException,
                                                              IllegalAccessException {
        Map<String, String> browserFilter = new HashMap<>();
        browserFilter.put("Browser", "firefox");

        List<BrowserStackDevice> filteredDevices = (List<BrowserStackDevice>) applyFilters.invoke(
                null, browserStackDevices, browserFilter);

        assertEquals(1540, filteredDevices.size());

        browserFilter.replace("Browser", "Firefox");
        filteredDevices = (List<BrowserStackDevice>) applyFilters.invoke(null, browserStackDevices,
                                                                         browserFilter);
        assertEquals(1540, filteredDevices.size());
    }

    @Test
    void userCanGetDeviceAfterApplyingDeviceNameFilter() throws InvocationTargetException,
                                                                IllegalAccessException,
                                                                IOException {
        Map<String, String> deviceNameFilter = new LinkedHashMap<>();
        deviceNameFilter.put("Device", "samsung");

        List<BrowserStackDevice> filteredDevices = (List<BrowserStackDevice>) applyFilters.invoke(
                null, browserStackDevices, deviceNameFilter);

        assertEquals(77, filteredDevices.size());

        deviceNameFilter.replace("Device", "Samsung");

        filteredDevices = (List<BrowserStackDevice>) applyFilters.invoke(null, browserStackDevices,
                                                                         deviceNameFilter);

        assertEquals(77, filteredDevices.size());
    }

    @Test
    void userCanGetDevicesAfterApplyingBrowserVersionFilter() throws InvocationTargetException,
                                                                     IllegalAccessException {
        Map<String, String> browserVersionFilter = new HashMap<>();
        browserVersionFilter.put("Browser_version", "95.0");

        List<BrowserStackDevice> browserVersionFilteredDevices = (List<BrowserStackDevice>) applyFilters.invoke(
                null, browserStackDevices, browserVersionFilter);

        assertEquals(37, browserVersionFilteredDevices.size());
    }

    @Test
    void userCanGetDevicesThatAreOnlyMobile() throws InvocationTargetException, IllegalAccessException {
        Map<String, String> mobileDeviceFilter = new HashMap<>();
        mobileDeviceFilter.put("Platform", "mobile");

        List<BrowserStackDevice> mobileDevices = (List<BrowserStackDevice>) applyFilters.invoke(
                null, browserStackDevices, mobileDeviceFilter);
        assertEquals(193, mobileDevices.size());
    }
}
