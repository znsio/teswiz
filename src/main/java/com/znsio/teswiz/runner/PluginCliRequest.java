package com.znsio.teswiz.runner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

public class PluginCliRequest {
    private static final Logger LOGGER = LogManager.getLogger(PluginCliRequest.class.getName());

    public static PluginCli getCliArgs() {
        AppiumServerManager appiumServerManager = new AppiumServerManager();
        String remoteWDHubIP = appiumServerManager.getRemoteWDHubIP();
        URL url = null;
        try {
            url = new URL(remoteWDHubIP);
            String response = Api.getResponse(url.getProtocol() + "://" + url.getHost()
                                              + ":" + url.getPort() + "/device-farm/api/cliArgs");
            final PluginCli[] pluginClIS = new ObjectMapper().readValue(response, PluginCli[].class);
            return pluginClIS[0];
        } catch (MalformedURLException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}