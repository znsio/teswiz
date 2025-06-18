package com.znsio.teswiz.runner;

import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class AppPathTest {
    private static final String LOG_DIR = "./target/testLogs";
    private static final String directoryPath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + "unitTests" + File.separator + "sampleApps";
    private static final String fileName = "VodQA.apk";
    private static final String expectedAppPath = directoryPath + File.separator + fileName;
    private static final String appPathAsCorrectUrl = "https://github.com/anandbagmar/sampleAppsForNativeMobileAutomation/raw/main/VodQA.apk";
    private static final String appPathAsIncorrectUrl = "https://github.com/anandbagmar/sampleAppsForNativeMobileAutomation/ra/main/VodQA.apk";
    private static final String appPathAsCorrectFilePath = expectedAppPath;
    private static final String appPathAsIncorrectFilePath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + "unitTests" + File.separator + "smleApps" + File.separator + fileName;

    @BeforeAll
    public static void setupBefore() {
        System.setProperty("LOG_DIR", LOG_DIR);
        FileUtils.createDirectory(LOG_DIR);
    }

    @Test
    void givenIncorrectUrl_WhenDirectoryAndFileDoNotExist_ThenIOExceptionOccurWhileTryingToDownloadFile() {
        deleteDirectoryUsedForUnitTests();
        assertThrows(InvalidTestDataException.class, () -> DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsIncorrectUrl, directoryPath));
    }

    @Test
    void givenIncorrectUrl_WhenDirectoryExistAndFileDoNotExist_ThenIOExceptionOccurWhileTryingToDownloadFile() {
        createDirectoryUsedForUnitTests();
        deleteFile(appPathAsCorrectFilePath);
        assertThrows(InvalidTestDataException.class, () -> DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsIncorrectUrl, directoryPath));
    }

    @Test
    void givenIncorrectUrl_WhenDirectoryAndFileBothExist_ThenFileIsReadable() {
        createDirectoryUsedForUnitTests();
        DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectUrl, directoryPath);
        assertThrows(InvalidTestDataException.class, () -> DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsIncorrectUrl, directoryPath));
    }

    @Test
    void givenCorrectUrl_WhenDirectoryAndFileDoNotExist_ThenCreateDirectoryAndDownloadFile() {
        deleteDirectoryUsedForUnitTests();
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectUrl, directoryPath);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(new File(actualAppPath).canRead());
    }

    @Test
    void givenCorrectUrl_WhenDirectoryExistButFileDoNotExist_ThenDownloadFile() {
        createDirectoryUsedForUnitTests();
        deleteFile(appPathAsCorrectFilePath);
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectUrl, directoryPath);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(new File(actualAppPath).canRead());
    }

    @Test
    void givenCorrectUrl_WhenDirectoryAndFileAlreadyExist_ThenDoNotDownloadFile() {
        createDirectoryUsedForUnitTests();
        DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectUrl, directoryPath);
        assertTrue(new File(expectedAppPath).canRead());
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectUrl, directoryPath);
        assertTrue(new File(actualAppPath).canRead());
        assertEquals(expectedAppPath, actualAppPath);
    }

    @Test
    void givenIncorrectFilePath_WhenDirectoryAndFileDoNotExist_ThenFileIsNotReadable() {
        deleteDirectoryUsedForUnitTests();
        assertThrows(InvalidTestDataException.class, () -> DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsIncorrectFilePath, directoryPath));
    }

    @Test
    void givenIncorrectFilePath_WhenDirectoryExistButFileDoNotExist_ThenFileIsNotReadable() {
        createDirectoryUsedForUnitTests();
        deleteFile(appPathAsCorrectFilePath);
        assertThrows(InvalidTestDataException.class, () -> DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsIncorrectFilePath, directoryPath));
    }

    @Test
    void givenIncorrectFilePath_WhenDirectoryAndFileExist_ThenFileIsNotReadable() {
        createDirectoryUsedForUnitTests();
        assertThrows(InvalidTestDataException.class, () -> DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsIncorrectFilePath, directoryPath));
    }

    @Test
    void givenCorrectFilePath_WhenDirectoryAndFileDoNotExist_ThenFileIsNotReadable() {
        deleteDirectoryUsedForUnitTests();
        assertThrows(RuntimeException.class, () -> DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectFilePath, directoryPath));
    }

    @Test
    void givenCorrectFilePath_WhenDirectoryExistButFileDoNotExist_ThenFileIsNotReadable() {
        createDirectoryUsedForUnitTests();
        deleteFile(appPathAsCorrectFilePath);
        assertThrows(RuntimeException.class, () -> DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectFilePath, directoryPath));
    }

    @Test
    void givenCorrectFilePath_WhenDirectoryAndFileAlreadyExist_ThenFileIsReadable() {
        createDirectoryUsedForUnitTests();
        DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectUrl, directoryPath);
        String actualAppPath = DeviceSetup.downloadAppToDirectoryIfNeeded(appPathAsCorrectFilePath, directoryPath);
        assertTrue(new File(actualAppPath).canRead());
        assertEquals(expectedAppPath, actualAppPath);
    }

    private void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file for unit test: " + e.getMessage());
        }
    }

    private void deleteDirectoryUsedForUnitTests() {
        if (Files.exists(Paths.get(AppPathTest.directoryPath))) {
            try {
                Files.walk(Paths.get(AppPathTest.directoryPath))
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(java.io.File::delete);
            } catch (IOException e) {
                System.err.println("Failed to delete folder for unit test: " + e.getMessage());
            }
        }
    }

    private void createDirectoryUsedForUnitTests() {
        if (!Files.exists(Paths.get(AppPathTest.directoryPath))) {
            try {
                Files.createDirectories(Paths.get(AppPathTest.directoryPath));
            } catch (IOException e) {
                System.err.println("Failed to create directory: " + AppPathTest.directoryPath + " for unit test, error occurred" + e);
            }
        }
    }
}
