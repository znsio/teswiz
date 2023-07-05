package com.znsio.teswiz.runner;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class AppPathTest {
    private static final String expectedDirectoryPath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + "sampleApps";
    private static final String fileName = "VodQA.apk";
    private static final String expectedAppPath = expectedDirectoryPath + File.separator + fileName;
    private static final String appPathAsUrl = "https://github.com/anandbagmar/sampleAppsForNativeMobileAutomation/raw/main/VodQA.apk";
    private static final String appPathAsIncorrectUrl = "https://github.com/anandbagmar/sampleAppsForNativeMobileAutomation/ra/main/VodQA.apk";
    private static final String appPathAsFilePath = expectedAppPath;
    private static final String appPathAsIncorrectFilePath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + "smleApps" + File.separator + fileName;

    @Test
    void givenCorrectUrl_WhenRepoAndFileDoNotExist_ThenCreateRepoAndDownloadFile() {
        deleteDirectorySubDirectoryAndFiles(expectedDirectoryPath);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsUrl);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(Files.exists(Paths.get(actualAppPath)));
    }

    @Test
    void givenCorrectUrl_WhenRepoAndFileAlreadyExist_ThenDoNotDownloadFile() {
        createDirectory(expectedDirectoryPath);
        DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsUrl);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsUrl);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(Files.exists(Paths.get(actualAppPath)));
    }

    @Test
    void givenCorrectUrl_WhenRepoExistButFileDoNotExist_ThenDownloadFile() {
        deleteFile(expectedAppPath);
        createDirectory(expectedDirectoryPath);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsUrl);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(Files.exists(Paths.get(actualAppPath)));
    }

    @Test
    void givenIncorrectUrl_WhenRepoAndFileDoNotExist_ThenIOExceptionOccurWhileTryingToDownloadFile() {
        deleteFile(expectedAppPath);
        assertThrows(RuntimeException.class, () -> DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsIncorrectUrl));
    }

    @Test
    void givenCorrectFilePath_WhenRepoAndFileAlreadyExist_ThenFileLoadsSuccessfully() {
        DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsUrl);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsFilePath);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(Files.exists(Paths.get(actualAppPath)));
        assertDoesNotThrow(()-> new FileInputStream(actualAppPath));
    }

    @Test
    void givenIncorrectFilePath_WhenRepoAndFileExist_ThenFileNotFoundExceptionOccursWhileTryingToOpenFile() {
        createDirectory(expectedDirectoryPath);
        DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsUrl);
        assertThrows(RuntimeException.class, () -> DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsIncorrectFilePath));
    }

    @Test
    void givenCorrectFilePath_WhenRepoExistButFileDoNotExist_ThenFileNotFoundExceptionOccursWhileTryingToOpenFile() {
        deleteFile(expectedAppPath);
        assertThrows(RuntimeException.class, () -> DeviceSetup.convertAppPathToFilePathIfNeeded(expectedAppPath));
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
