package com.znsio.teswiz.runner;

import com.znsio.teswiz.mobile.server.AppiumServerController;

@Deprecated(forRemoval = false)
public class AppiumServerManager {
    private final AppiumServerController controller;

    public AppiumServerManager() {
        this(new AppiumServerController(DeviceSetup::getCloudUrlFromCapabilities));
    }

    AppiumServerManager(AppiumServerController controller) {
        this.controller = controller;
    }

    public static void destroyAppiumNode() {
        AppiumServerController.destroyAppiumNode();
    }

    public String getRemoteWDHubIP() {
        return controller.getRemoteWDHubIP();
    }

    public void startAppiumServer(String host) {
        controller.startAppiumServer(host);
    }
}
