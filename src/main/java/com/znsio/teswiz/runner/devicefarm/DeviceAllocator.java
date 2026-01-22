package com.znsio.teswiz.runner.devicefarm;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class DeviceAllocator {

    private final Path locksDir;

    /**
     * You can override lock dir via system property: teswiz.devicefarm.lockDir
     */
    public DeviceAllocator() {
        this(resolveLockDir());
    }

    public DeviceAllocator(Path locksDir) {
        this.locksDir = locksDir;
    }

    public Path locksDir() {
        return locksDir;
    }

    public Optional<DeviceLease> tryAcquire(List<Device> devices, Predicate<Device> filter) {
        return devices.stream()
                .filter(Device::isOnline)
                .filter(filter)
                .sorted(Comparator.comparing(Device::platform)
                                .thenComparing(Device::kind)
                                .thenComparing(Device::id))
                .map(d -> DeviceLease.tryAcquire(d, locksDir))
                .filter(l -> l != null)
                .findFirst();
    }

    public Optional<DeviceLease> acquire(List<Device> devices,
            Predicate<Device> filter,
            Duration timeout,
            Duration pollInterval) {
        Instant end = Instant.now().plus(timeout);
        while (Instant.now().isBefore(end)) {
            Optional<DeviceLease> lease = tryAcquire(devices, filter);
            if (lease.isPresent()) {
                return lease;
            }

            try {
                Thread.sleep(Math.max(50, pollInterval.toMillis()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static Path resolveLockDir() {
        String configured = System.getProperty("teswiz.devicefarm.lockDir");
        if (configured != null && !configured.trim().isEmpty()) {
            return Path.of(configured.trim());
        }
        // CI/VM friendly default
        return Path.of(System.getProperty("java.io.tmpdir"), "teswiz-device-locks");
    }
}
