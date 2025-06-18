package com.znsio.teswiz.tools;

import com.znsio.teswiz.exceptions.InvalidTestDataException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class FileUtils {

    private static final Logger LOGGER = LogManager.getLogger(FileUtils.class.getName());

    public static boolean createParentDirectory(String parent, String child) {
        validatePathComponent(parent, "Parent directory");
        validatePathComponent(child, "Child directory");
        return createDirectoryInternal(new File(parent, child));
    }

    public static boolean createDirectory(String dir) {
        validatePathComponent(dir, "Directory");
        return createDirectoryInternal(new File(dir));
    }

    private static boolean createDirectoryInternal(File file) {
        if (!file.exists()) {
            boolean created = file.mkdirs();
            LOGGER.debug("Directory: {} created: {}", file.getAbsolutePath(), created);
            return created;
        }
        LOGGER.debug("Directory already exists: {}", file.getAbsolutePath());
        return true;
    }

    private static void validatePathComponent(String path, String label) {
        if (path == null || path.trim().isEmpty()) {
            throw new InvalidTestDataException(label + " is null or empty");
        }
    }
}
