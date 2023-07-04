package com.znsio.teswiz.runner;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class AppPathTest {
    private static final String tempFolderPath = System.getProperty("user.dir") + File.separator + "temp";
    private static final String expectedDirectoryPath = tempFolderPath + File.separator + "sampleApps";
    private static final String fileName = "VodQA.apk";
    private static final String expectedAppPath = expectedDirectoryPath + File.separator + fileName;
    private static final String appPathAsUrl = "https://github.com/anandbagmar/sampleAppsForNativeMobileAutomation/raw/main/VodQA.apk";
    private static final String appPathAsIncorrectUrl = "https://github.com/anandbagmar/sampleAppsForNativeMobileAutomation/ra/main/VodQA.apk";
    private static final String appPathAsFilePath = expectedAppPath;
    private static final String appPathAsIncorrectFilePath = tempFolderPath + File.separator + "smleApps" + File.separator + fileName;

    @Test
    void validateAppPathAsCorrectUrlCreateRepoAndDownloadFileWhenBothDoNotExist() {
        deleteDirectorySubDirectoryAndFiles(tempFolderPath);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsUrl);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(Files.exists(Paths.get(actualAppPath)));
    }

    @Test
    void validateAppPathAsCorrectUrlDoNotDownloadFileWhenRepoAndFileAlreadyExist() {
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsUrl);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(Files.exists(Paths.get(actualAppPath)));
    }

    @Test
    void validateAppPathAsCorrectUrlOnlyDownloadFileWhenRepoExist() {
        deleteFile(expectedAppPath);
        createDirectory(expectedDirectoryPath);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsUrl);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(Files.exists(Paths.get(actualAppPath)));
    }

    @Test
    void validateAppPathAsIncorrectUrlGivesError() {
        deleteDirectorySubDirectoryAndFiles(tempFolderPath);
        assertThrows(RuntimeException.class, () -> DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsIncorrectUrl));
    }

    @Test
    void validateAppPathAsCorrectFilePathLoadsFileWhenAlreadyExist() {
        DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsUrl);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsFilePath);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(Files.exists(Paths.get(actualAppPath)));
    }

    @Test
    void validateAppPathAsIncorrectFilePathGivesError() {
        DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsUrl);
        assertThrows(RuntimeException.class, () -> DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsIncorrectFilePath));
    }

    @Test
    void validateAppPathAsFilePathButFileNotPresentGivesError() {
        deleteFile(expectedAppPath);
        assertThrows(RuntimeException.class, () -> DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsIncorrectFilePath));
    }

    private void deleteFile(String filePath) {
        if (Files.exists(Path.of(filePath))) {
            try {
                Files.delete(Path.of(filePath));
            } catch (IOException e) {
                System.err.println("Failed to delete file for unit test: " + e.getMessage());
            }
        }
    }

    private void deleteDirectorySubDirectoryAndFiles(String directoryPath) {
        if (Files.exists(Paths.get(directoryPath))) {
            try {
                Files.walk(Paths.get(directoryPath))
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(java.io.File::delete);
            } catch (IOException e) {
                System.err.println("Failed to delete folder for unit test: " + e.getMessage());
            }
        }
    }

    private void createDirectory(String directoryPath) {
        if (!Files.exists(Paths.get(directoryPath))) {
            try {
                Files.createDirectories(Paths.get(directoryPath));
            } catch (IOException e) {
                System.err.println("Failed to create directory: " + expectedDirectoryPath + " for unit test, error occurred" + e);
            }
        }
    }
}
