package com.znsio.teswiz.runner.atd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.Device;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Devices {
    private static final Logger LOGGER = LogManager.getLogger(Devices.class.getName());
    private static List<Device> instance;

    private Devices() {

    }

    public static List<Device> getConnectedDevices() {
        if (instance == null) {
            System.out.println(Thread.currentThread().getId());
            ATD_AppiumServerManager appiumServerManager = new ATD_AppiumServerManager();
            String remoteWDHubIP = appiumServerManager.getRemoteWDHubIP();
            URL url = null;
            try {
                url = new URL(remoteWDHubIP);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            String response = new Api().getResponse(url.getProtocol()
                                                    + "://" + url.getHost() + ":" + url.getPort() + "/device-farm/api/device");
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                instance = Arrays.asList(mapper.readValue(response, Device[].class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }
}
