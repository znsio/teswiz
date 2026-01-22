package com.znsio.teswiz.runner.devicefarm;

import java.util.ArrayList;
import java.util.List;

public final class DeviceDiscoveryService {

    private final AndroidDeviceDiscovery android;
    private final IosSimulatorDiscovery iosSims;
    private final IosRealDeviceDiscovery iosReal; // optional

    public DeviceDiscoveryService(boolean androidEnrichWithGetProp, boolean includeIosRealDevices) {
        this.android = new AndroidDeviceDiscovery(androidEnrichWithGetProp);
        this.iosSims = new IosSimulatorDiscovery();
        this.iosReal = includeIosRealDevices ? new IosRealDeviceDiscovery() : null;
    }

    public List<Device> discoverAll() {
        List<Device> out = new ArrayList<>();
        out.addAll(android.discover());
        out.addAll(iosSims.discover());
        if (iosReal != null) {
            out.addAll(iosReal.discover());
        }
        return out;
    }
}
