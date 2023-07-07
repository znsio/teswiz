package com.znsio.teswiz.runner;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class AppPathTest {
    private static final String expectedDirectoryPath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + "unitTests" + File.separator + "sampleApps";
    private static final String appPathAsCorrectDirectoryPath = expectedDirectoryPath;
    private static final String fileName = "VodQA.apk";
    private static final String expectedAppPath = expectedDirectoryPath + File.separator + fileName;
    private static final String appPathAsCorrectUrl = "https://github.com/anandbagmar/sampleAppsForNativeMobileAutomation/raw/main/VodQA.apk";
    private static final String appPathAsIncorrectUrl = "https://github.com/anandbagmar/sampleAppsForNativeMobileAutomation/ra/main/VodQA.apk";
    private static final String appPathAsCorrectFilePath = expectedAppPath;
    private static final String appPathAsIncorrectFilePath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + "unitTests" + File.separator + "smleApps" + File.separator + fileName;

    @Test
    void givenIncorrectUrl_WhenRepoAndFileDoNotExist_ThenIOExceptionOccurWhileTryingToDownloadFile() {
        deleteDirectorySubDirectoryAndFiles(appPathAsCorrectDirectoryPath);
        assertThrows(RuntimeException.class, () -> DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsIncorrectUrl, appPathAsCorrectDirectoryPath));
    }

    @Test
    void givenIncorrectUrl_WhenRepoExistAndFileDoNotExist_ThenIOExceptionOccurWhileTryingToDownloadFile() {
        createDirectory(appPathAsCorrectDirectoryPath);
        deleteFile(appPathAsCorrectFilePath);
        assertThrows(RuntimeException.class, () -> DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsIncorrectUrl, appPathAsCorrectDirectoryPath));
    }

    @Test
    void givenIncorrectUrl_WhenRepoAndFileBothExist_ThenFileIsReadable() {
        createDirectory(appPathAsCorrectDirectoryPath);
        DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsCorrectUrl, appPathAsCorrectDirectoryPath);
        assertTrue(new File(expectedAppPath).canRead());
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsIncorrectUrl, appPathAsCorrectDirectoryPath);
        assertTrue(new File(actualAppPath).canRead());
        assertEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenCorrectUrl_WhenRepoAndFileDoNotExist_ThenCreateRepoAndDownloadFile() {
        deleteDirectorySubDirectoryAndFiles(appPathAsCorrectDirectoryPath);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsCorrectUrl, appPathAsCorrectDirectoryPath);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(new File(actualAppPath).canRead());
    }

    @Test
    void givenCorrectUrl_WhenRepoExistButFileDoNotExist_ThenDownloadFile() {
        createDirectory(appPathAsCorrectDirectoryPath);
        deleteFile(appPathAsCorrectFilePath);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsCorrectUrl, appPathAsCorrectDirectoryPath);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(new File(actualAppPath).canRead());
    }

    @Test
    void givenCorrectUrl_WhenRepoAndFileAlreadyExist_ThenDoNotDownloadFile() {
        createDirectory(appPathAsCorrectDirectoryPath);
        DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsCorrectUrl, appPathAsCorrectDirectoryPath);
        assertTrue(new File(expectedAppPath).canRead());
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsCorrectUrl, expectedDirectoryPath);
        assertTrue(new File(actualAppPath).canRead());
        assertEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenIncorrectFilePath_WhenRepoAndFileDoNotExist_ThenFileIsNotReadable() {
        deleteDirectorySubDirectoryAndFiles(appPathAsCorrectDirectoryPath);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsIncorrectFilePath, appPathAsCorrectDirectoryPath);
        assertFalse(new File(actualAppPath).canRead());
        assertNotEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenIncorrectFilePath_WhenRepoExistButFileDoNotExist_ThenFileIsNotReadable() {
        createDirectory(appPathAsCorrectDirectoryPath);
        deleteFile(appPathAsCorrectFilePath);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsIncorrectFilePath, appPathAsCorrectDirectoryPath);
        assertFalse(new File(actualAppPath).canRead());
        assertNotEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenIncorrectFilePath_WhenRepoAndFileExist_ThenFileIsNotReadable() {
        createDirectory(expectedDirectoryPath);
        DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsCorrectUrl, appPathAsCorrectDirectoryPath);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsIncorrectFilePath, appPathAsCorrectDirectoryPath);
        assertFalse(new File(actualAppPath).canRead());
        assertNotEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenCorrectFilePath_WhenRepoAndFileDoNotExist_ThenFileIsNotReadable() {
        deleteDirectorySubDirectoryAndFiles(appPathAsCorrectDirectoryPath);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsCorrectFilePath, appPathAsCorrectDirectoryPath);
        assertFalse(new File(actualAppPath).canRead());
        assertEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenCorrectFilePath_WhenRepoExistButFileDoNotExist_ThenFileIsNotReadable() {
        createDirectory(appPathAsCorrectDirectoryPath);
        deleteFile(appPathAsCorrectFilePath);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsCorrectFilePath, appPathAsCorrectDirectoryPath);
        assertFalse(new File(actualAppPath).canRead());
        assertEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenCorrectFilePath_WhenRepoAndFileAlreadyExist_ThenFileIsReadable() {
        createDirectory(appPathAsCorrectDirectoryPath);
        DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsCorrectUrl, appPathAsCorrectDirectoryPath);
        String actualAppPath = DeviceSetup.convertAppPathToFilePathIfNeeded(appPathAsCorrectFilePath, appPathAsCorrectDirectoryPath);
        assertTrue(new File(actualAppPath).canRead());
        assertEquals(expectedAppPath, actualAppPath);
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
