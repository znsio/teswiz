package com.znsio.teswiz.runner.devicefarm;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public final class DeviceLease implements AutoCloseable {

    private final Device device;
    private final Path lockFile;
    private final FileChannel channel;
    private final FileLock lock;

    private DeviceLease(Device device, Path lockFile, FileChannel channel, FileLock lock) {
        this.device = device;
        this.lockFile = lockFile;
        this.channel = channel;
        this.lock = lock;
    }

    public Device device() {
        return device;
    }

    /**
     * Try to acquire a lock for the device (atomic across JVM processes).
     * Returns null if already locked.
     */
    public static DeviceLease tryAcquire(Device device, Path locksDir) {
        try {
            Files.createDirectories(locksDir);

            String safeId = device.id().replaceAll("[^a-zA-Z0-9._-]", "_");
            Path lf = locksDir.resolve(device.platform() + "-" + safeId + ".lock");

            RandomAccessFile raf = new RandomAccessFile(lf.toFile(), "rw");
            FileChannel ch = raf.getChannel();
            FileLock fl = ch.tryLock();
            if (fl == null) {
                ch.close();
                return null;
            }

            // Debug metadata
            String meta = "lockedAt=" + Instant.now() + "\n" +
                          "pid=" + ProcessHandle.current().pid() + "\n" +
                          "device=" + device + "\n";

            raf.setLength(0);
            raf.write(meta.getBytes(StandardCharsets.UTF_8));
            raf.getFD().sync();

            return new DeviceLease(device, lf, ch, fl);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void close() {
        try {
            lock.release();
        } catch (Exception ignored) {
        }
        try {
            channel.close();
        } catch (Exception ignored) {
        }
        try {
            Files.deleteIfExists(lockFile);
        } catch (Exception ignored) {
        }
    }
}
