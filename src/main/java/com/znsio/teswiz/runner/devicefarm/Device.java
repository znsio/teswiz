package com.znsio.teswiz.runner.devicefarm;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class Device {
    public enum Platform {ANDROID, IOS}

    public enum Kind {REAL, EMULATOR, SIMULATOR}

    public enum State {ONLINE, OFFLINE, UNAUTHORIZED, UNKNOWN}

    private final String id;            // Android: adb serial | iOS: udid
    private final Platform platform;
    private final Kind kind;

    private final String name;          // friendly name (best effort)
    private final String osVersion;     // "14", "17.2", etc (best effort)
    private final String manufacturer;  // best effort
    private final String model;         // best effort

    private final State state;
    private final Map<String, String> meta;

    public Device(
            String id,
            Platform platform,
            Kind kind,
            String name,
            String osVersion,
            String manufacturer,
            String model,
            State state,
            Map<String, String> meta
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.platform = Objects.requireNonNull(platform, "platform");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.name = name;
        this.osVersion = osVersion;
        this.manufacturer = manufacturer;
        this.model = model;
        this.state = state == null ? State.UNKNOWN : state;
        this.meta = meta == null ? Collections.emptyMap() : Collections.unmodifiableMap(meta);
    }

    public String id() {
        return id;
    }

    public Platform platform() {
        return platform;
    }

    public Kind kind() {
        return kind;
    }

    public String name() {
        return name;
    }

    public String osVersion() {
        return osVersion;
    }

    public String manufacturer() {
        return manufacturer;
    }

    public String model() {
        return model;
    }

    public State state() {
        return state;
    }

    public boolean isOnline() {
        return state == State.ONLINE;
    }

    public Map<String, String> meta() {
        return meta;
    }

    @Override
    public String toString() {
        return "Device{" +
               "id='" + id + '\'' +
               ", platform=" + platform +
               ", kind=" + kind +
               ", name='" + name + '\'' +
               ", osVersion='" + osVersion + '\'' +
               ", manufacturer='" + manufacturer + '\'' +
               ", model='" + model + '\'' +
               ", state=" + state +
               ", meta=" + meta +
               '}';
    }
}
