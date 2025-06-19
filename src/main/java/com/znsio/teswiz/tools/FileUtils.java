package com.znsio.teswiz.tools;

import com.znsio.teswiz.exceptions.InvalidTestDataException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    private static final Logger LOGGER = LogManager.getLogger(FileUtils.class.getName());

    public synchronized static File createDirectoryIn(String parent, String child) {
        validatePathComponent("Parent", parent);
        validatePathComponent("Child", child);

        File file = new File(parent, child);
        return createDirectoryInternal(file);
    }

    public static File createDirectory(String dir) {
        validatePathComponent("Directory", dir);
        File file = new File(dir);
        return createDirectoryInternal(file);
    }

    private static File createDirectoryInternal(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                throw new InvalidTestDataException("Path exists but is a file: " + file.getAbsolutePath());
            }
            return file;
        }

        boolean created = false;
        if (looksLikeAFile(file.getName())) {
            LOGGER.warn("Path '{}' looks like a file name. Creating directory till its parent", file.getAbsolutePath());
            created = file.getParentFile().mkdirs();
        } else {
            created = file.mkdirs();
        }
        LOGGER.debug("Directory: {} created?: {}", file.getAbsolutePath(), created);
        return file;
    }

    private static void validatePathComponent(String name, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidTestDataException(name + " is null or empty");
        }
    }

    private static boolean looksLikeAFile(String name) {
        // crude check for extensions (e.g., file.txt, data.json)
        return name.matches(".*\\.[a-zA-Z0-9]+$");
    }

    public static void copyFile(File source, File destination) {
        try {
            org.apache.commons.io.FileUtils.copyFile(source, destination);
        } catch (IOException e) {
            LOGGER.warn("ERROR: Unable to copy file from '{}' to '{}'\n", source.getAbsolutePath(), destination.getAbsolutePath());
            LOGGER.debug(ExceptionUtils.getStackTrace(e));
        }
    }
}
