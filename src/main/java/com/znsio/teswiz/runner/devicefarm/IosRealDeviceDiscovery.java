package com.znsio.teswiz.runner.devicefarm;

import com.znsio.teswiz.tools.OsUtils;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.znsio.teswiz.runner.devicefarm.Device.*;

public final class IosRealDeviceDiscovery {

    private static final int IDEVICE_TIMEOUT = 12;

    public List<Device> discover() {
        if (!OsUtils.isMac()) {
            return List.of();
        }

        if (!CommandLineExecutor.toolWorks("idevice_id")) {
            return List.of();
        }

        CommandLineResponse ids = CommandLineExecutor.execCommand(new String[]{"idevice_id", "-l"}, IDEVICE_TIMEOUT);
        if (ids.getExitCode() != 0 || ids.isTimedOut() || ids.getStdOut() == null || ids.getStdOut().isBlank()) {
            return List.of();
        }

        List<Device> out = new ArrayList<>();
        for (String udid : ids.getStdOut().split("\\R")) {
            udid = udid.trim();
            if (udid.isEmpty()) {
                continue;
            }

            String name = ideviceInfo(udid, "DeviceName");
            String productType = ideviceInfo(udid, "ProductType");
            String osVersion = ideviceInfo(udid, "ProductVersion");

            Map<String, String> meta = new LinkedHashMap<>();
            if (productType != null) {
                meta.put("ProductType", productType);
            }

            out.add(new Device(
                    udid,
                    Platform.IOS,
                    Kind.REAL,
                    name != null ? name : productType,
                    osVersion,
                    "Apple",
                    productType,
                    State.ONLINE,
                    meta
            ));
        }

        return out;
    }

    private static String ideviceInfo(String udid, String key) {
        if (!CommandLineExecutor.toolWorks("ideviceinfo")) {
            return null;
        }
        CommandLineResponse r = CommandLineExecutor.execCommand(new String[]{"ideviceinfo", "-u", udid, "-k", key}, IDEVICE_TIMEOUT);
        if (r.getExitCode() != 0 || r.isTimedOut() || r.getStdOut() == null) {
            return null;
        }
        String v = r.getStdOut().trim();
        return v.isEmpty() ? null : v;
    }
}
