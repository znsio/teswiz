package com.znsio.teswiz.runner.devicefarm;

import com.znsio.teswiz.tools.OsUtils;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.znsio.teswiz.runner.devicefarm.Device.*;

public final class IosSimulatorDiscovery {

    private static final int SIMCTL_TIMEOUT = 20;

    private static final Pattern RUNTIME_BLOCK =
            Pattern.compile("\"(com\\.apple\\.CoreSimulator\\.SimRuntime\\.[^\"]+)\"\\s*:\\s*\\[(.*?)\\]\\s*(,|\\})", Pattern.DOTALL);

    private static final Pattern DEVICE_OBJ =
            Pattern.compile("\\{(.*?)\\}\\s*(,|$)", Pattern.DOTALL);

    public List<Device> discover() {
        if (!OsUtils.isMac()) {
            return List.of();
        }
        if (!CommandLineExecutor.toolWorks("xcrun")) {
            return List.of();
        }

        CommandLineResponse r = CommandLineExecutor.execCommand(
                new String[]{"xcrun", "simctl", "list", "devices", "--json"},
                SIMCTL_TIMEOUT
        );

        if (r.getExitCode() != 0 || r.isTimedOut() || r.getStdOut() == null || r.getStdOut().isBlank()) {
            return List.of();
        }

        String json = r.getStdOut();
        List<Device> out = new ArrayList<>();

        Matcher runtimeMatcher = RUNTIME_BLOCK.matcher(json);
        while (runtimeMatcher.find()) {
            String runtime = runtimeMatcher.group(1);
            String arr = runtimeMatcher.group(2);

            String osVersion = runtimeToOsVersion(runtime);

            Matcher devMatcher = DEVICE_OBJ.matcher(arr);
            while (devMatcher.find()) {
                String obj = devMatcher.group(1);

                String name = extract(obj, "name");
                String udid = extract(obj, "udid");
                String state = extract(obj, "state");
                String isAvailable = extract(obj, "isAvailable");

                if (udid == null || udid.isBlank()) {
                    continue;
                }

                State st;
                if ("Booted".equalsIgnoreCase(state)) {
                    st = State.ONLINE;
                } else if ("Shutdown".equalsIgnoreCase(state)) {
                    st = State.OFFLINE;
                } else {
                    st = State.UNKNOWN;
                }

                Map<String, String> meta = new LinkedHashMap<>();
                meta.put("runtime", runtime);
                if (state != null) {
                    meta.put("simState", state);
                }
                if (isAvailable != null) {
                    meta.put("isAvailable", isAvailable);
                }

                out.add(new Device(
                        udid,
                        Platform.IOS,
                        Kind.SIMULATOR,
                        name,
                        osVersion,
                        "Apple",
                        name,
                        st,
                        meta
                ));
            }
        }

        return out;
    }

    private static String extract(String obj, String key) {
        Pattern pStr = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher mStr = pStr.matcher(obj);
        if (mStr.find()) {
            return mStr.group(1);
        }

        Pattern pBool = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false|null)");
        Matcher mBool = pBool.matcher(obj);
        if (mBool.find()) {
            return mBool.group(1);
        }

        return null;
    }

    private static String runtimeToOsVersion(String runtime) {
        int idx = runtime.lastIndexOf("iOS-");
        if (idx < 0) {
            return null;
        }
        return runtime.substring(idx + 4).replace('-', '.');
    }
}
