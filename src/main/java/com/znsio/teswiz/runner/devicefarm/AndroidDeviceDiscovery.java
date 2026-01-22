package com.znsio.teswiz.runner.devicefarm;

import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.znsio.teswiz.runner.devicefarm.Device.*;

public final class AndroidDeviceDiscovery {

    private static final Pattern ADB_LINE =
            Pattern.compile("^(\\S+)\\s+(device|offline|unauthorized)\\s*(.*)$");

    private static final int ADB_LIST_TIMEOUT = 15;
    private static final int ADB_PROP_TIMEOUT = 8;

    private final boolean enrichWithGetProp;

    public AndroidDeviceDiscovery(boolean enrichWithGetProp) {
        this.enrichWithGetProp = enrichWithGetProp;
    }

    public List<Device> discover() {
        if (!CommandLineExecutor.toolWorks("adb")) {
            return List.of();
        }

        CommandLineResponse resp = CommandLineExecutor.execCommand(new String[]{"adb", "devices", "-l"}, ADB_LIST_TIMEOUT);
        if (resp.getExitCode() != 0 || resp.isTimedOut() || resp.getStdOut() == null) {
            return List.of();
        }

        List<Device> out = new ArrayList<>();
        for (String line : resp.getStdOut().split("\\R")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("List of devices attached")) {
                continue;
            }

            Matcher m = ADB_LINE.matcher(line);
            if (!m.matches()) {
                continue;
            }

            String serial = m.group(1);
            String stateToken = m.group(2);
            String rest = m.group(3);

            State state = switch (stateToken) {
                case "device" -> State.ONLINE;
                case "offline" -> State.OFFLINE;
                case "unauthorized" -> State.UNAUTHORIZED;
                default -> State.UNKNOWN;
            };

            boolean emulator = serial.startsWith("emulator-");
            Map<String, String> meta = parseKeyValues(rest);

            String manufacturer = null;
            String model = meta.get("model");
            String osVersion = null;

            if (state == State.ONLINE && enrichWithGetProp) {
                manufacturer = adbProp(serial, "ro.product.manufacturer");
                String modelProp = adbProp(serial, "ro.product.model");
                if (modelProp != null && !modelProp.isBlank()) {
                    model = modelProp;
                }
                osVersion = adbProp(serial, "ro.build.version.release");
            }

            String name = (model != null && !model.isBlank()) ? model : serial;

            out.add(new Device(
                    serial,
                    Platform.ANDROID,
                    emulator ? Kind.EMULATOR : Kind.REAL,
                    name,
                    osVersion,
                    manufacturer,
                    model,
                    state,
                    meta
            ));
        }

        return out;
    }

    private static Map<String, String> parseKeyValues(String s) {
        Map<String, String> m = new LinkedHashMap<>();
        if (s == null || s.isBlank()) {
            return m;
        }
        for (String tok : s.trim().split("\\s+")) {
            int idx = tok.indexOf(':');
            if (idx > 0 && idx < tok.length() - 1) {
                m.put(tok.substring(0, idx), tok.substring(idx + 1));
            }
        }
        return m;
    }

    private static String adbProp(String serial, String prop) {
        CommandLineResponse r = CommandLineExecutor.execCommand(
                new String[]{"adb", "-s", serial, "shell", "getprop", prop},
                ADB_PROP_TIMEOUT
        );
        if (r.getExitCode() != 0 || r.isTimedOut() || r.getStdOut() == null) {
            return null;
        }
        String v = r.getStdOut().trim();
        return v.isEmpty() ? null : v;
    }
}
