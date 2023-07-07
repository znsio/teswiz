package com.znsio.teswiz.runner;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class AppPathTest {
    private static final String directoryPath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + "unitTests" + File.separator + "sampleApps";
    private static final String fileName = "VodQA.apk";
    private static final String expectedAppPath = directoryPath + File.separator + fileName;
    private static final String appPathAsCorrectUrl = "https://github.com/anandbagmar/sampleAppsForNativeMobileAutomation/raw/main/VodQA.apk";
    private static final String appPathAsIncorrectUrl = "https://github.com/anandbagmar/sampleAppsForNativeMobileAutomation/ra/main/VodQA.apk";
    private static final String appPathAsCorrectFilePath = expectedAppPath;
    private static final String appPathAsIncorrectFilePath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + "unitTests" + File.separator + "smleApps" + File.separator + fileName;

    @Test
    void givenIncorrectUrl_WhenDirectoryAndFileDoNotExist_ThenIOExceptionOccurWhileTryingToDownloadFile() {
        deleteDirectorySubDirectoryAndFiles(directoryPath);
        assertThrows(RuntimeException.class, () -> DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsIncorrectUrl, directoryPath));
    }

    @Test
    void givenIncorrectUrl_WhenDirectoryExistAndFileDoNotExist_ThenIOExceptionOccurWhileTryingToDownloadFile() {
        createDirectory(directoryPath);
        deleteFile(appPathAsCorrectFilePath);
        assertThrows(RuntimeException.class, () -> DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsIncorrectUrl, directoryPath));
    }

    @Test
    void givenIncorrectUrl_WhenDirectoryAndFileBothExist_ThenFileIsReadable() {
        createDirectory(directoryPath);
        DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectUrl, directoryPath);
        assertTrue(new File(expectedAppPath).canRead());
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsIncorrectUrl, directoryPath);
        assertTrue(new File(actualAppPath).canRead());
        assertEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenCorrectUrl_WhenDirectoryAndFileDoNotExist_ThenCreateDirectoryAndDownloadFile() {
        deleteDirectorySubDirectoryAndFiles(directoryPath);
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectUrl, directoryPath);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(new File(actualAppPath).canRead());
    }

    @Test
    void givenCorrectUrl_WhenDirectoryExistButFileDoNotExist_ThenDownloadFile() {
        createDirectory(directoryPath);
        deleteFile(appPathAsCorrectFilePath);
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectUrl, directoryPath);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(new File(actualAppPath).canRead());
    }

    @Test
    void givenCorrectUrl_WhenDirectoryAndFileAlreadyExist_ThenDoNotDownloadFile() {
        createDirectory(directoryPath);
        DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectUrl, directoryPath);
        assertTrue(new File(expectedAppPath).canRead());
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectUrl, directoryPath);
        assertTrue(new File(actualAppPath).canRead());
        assertEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenIncorrectFilePath_WhenDirectoryAndFileDoNotExist_ThenFileIsNotReadable() {
        deleteDirectorySubDirectoryAndFiles(directoryPath);
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsIncorrectFilePath, directoryPath);
        assertFalse(new File(actualAppPath).canRead());
        assertNotEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenIncorrectFilePath_WhenDirectoryExistButFileDoNotExist_ThenFileIsNotReadable() {
        createDirectory(directoryPath);
        deleteFile(appPathAsCorrectFilePath);
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsIncorrectFilePath, directoryPath);
        assertFalse(new File(actualAppPath).canRead());
        assertNotEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenIncorrectFilePath_WhenDirectoryAndFileExist_ThenFileIsNotReadable() {
        createDirectory(directoryPath);
        DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectUrl, directoryPath);
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsIncorrectFilePath, directoryPath);
        assertFalse(new File(actualAppPath).canRead());
        assertNotEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenCorrectFilePath_WhenDirectoryAndFileDoNotExist_ThenFileIsNotReadable() {
        deleteDirectorySubDirectoryAndFiles(directoryPath);
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectFilePath, directoryPath);
        assertFalse(new File(actualAppPath).canRead());
        assertEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenCorrectFilePath_WhenDirectoryExistButFileDoNotExist_ThenFileIsNotReadable() {
        createDirectory(directoryPath);
        deleteFile(appPathAsCorrectFilePath);
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectFilePath, directoryPath);
        assertFalse(new File(actualAppPath).canRead());
        assertEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenCorrectFilePath_WhenDirectoryAndFileAlreadyExist_ThenFileIsReadable() {
        createDirectory(directoryPath);
        DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectUrl, directoryPath);
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectFilePath, directoryPath);
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
                System.err.println("Failed to create directory: " + directoryPath + " for unit test, error occurred" + e);
            }
        }
    }
}
