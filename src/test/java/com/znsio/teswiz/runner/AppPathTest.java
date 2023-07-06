package com.znsio.teswiz.runner;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class AppPathTest {
    private static final String LOG_DIR = "./target/testLogs";
    private static final String expectedDirectoryPath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + "unitTests" + File.separator + "sampleApps";
    private static final String fileName = "VodQA.apk";
    private static final String expectedAppPath = expectedDirectoryPath + File.separator + fileName;
    private static final String appPathAsUrl = "https://github.com/anandbagmar/sampleAppsForNativeMobileAutomation/raw/main/VodQA.apk";
    private static final String appPathAsIncorrectUrl = "https://github.com/anandbagmar/sampleAppsForNativeMobileAutomation/ra/main/VodQA.apk";
    private static final String appPathAsFilePath = expectedAppPath;
    private static final String appPathAsIncorrectFilePath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + "unitTests" + File.separator + "smleApps" + File.separator + fileName;

    @BeforeAll
    public static void setupBefore() {
        System.setProperty("LOG_DIR", LOG_DIR);
        new File(LOG_DIR).mkdirs();
    }

    @Test
    void givenCorrectUrl_WhenRepoAndFileDoNotExist_ThenCreateRepoAndDownloadFile() {
        deleteDirectorySubDirectoryAndFiles(expectedDirectoryPath);
        String actualAppPath = DeviceSetup.downloadAppAndGetFilePath(appPathAsUrl, expectedDirectoryPath);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(Files.exists(Paths.get(actualAppPath)));
    }

    @Test
    void givenCorrectUrl_WhenRepoAndFileAlreadyExist_ThenDoNotDownloadFile() {
        createDirectory(expectedDirectoryPath);
        DeviceSetup.downloadAppAndGetFilePath(appPathAsUrl, expectedDirectoryPath);
        String actualAppPath = DeviceSetup.downloadAppAndGetFilePath(appPathAsUrl, expectedDirectoryPath);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(Files.exists(Paths.get(actualAppPath)));
    }

    @Test
    void givenCorrectUrl_WhenRepoExistButFileDoNotExist_ThenDownloadFile() {
        deleteFile(expectedAppPath);
        createDirectory(expectedDirectoryPath);
        String actualAppPath = DeviceSetup.downloadAppAndGetFilePath(appPathAsUrl, expectedDirectoryPath);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(Files.exists(Paths.get(actualAppPath)));
    }

    @Test
    void givenIncorrectUrl_WhenRepoAndFileDoNotExist_ThenIOExceptionOccurWhileTryingToDownloadFile() {
        deleteFile(expectedAppPath);
        assertThrows(RuntimeException.class, () -> DeviceSetup.downloadAppAndGetFilePath(appPathAsIncorrectUrl, expectedDirectoryPath));
    }

    @Test
    void givenCorrectFilePath_WhenRepoAndFileAlreadyExist_ThenFileLoadsSuccessfully() {
        DeviceSetup.downloadAppAndGetFilePath(appPathAsUrl, expectedDirectoryPath);
        String actualAppPath = DeviceSetup.downloadAppAndGetFilePath(appPathAsFilePath, expectedDirectoryPath);
        assertEquals(expectedAppPath, actualAppPath);
        assertTrue(Files.exists(Paths.get(actualAppPath)));
        assertDoesNotThrow(() -> new FileInputStream(actualAppPath));
    }

    @Test
    void givenIncorrectFilePath_WhenRepoAndFileExist_ThenFileNotFoundExceptionOccursWhileTryingToOpenFile() {
        createDirectory(expectedDirectoryPath);
        DeviceSetup.downloadAppAndGetFilePath(appPathAsUrl, expectedDirectoryPath);
        assertThrows(RuntimeException.class, () -> DeviceSetup.downloadAppAndGetFilePath(appPathAsIncorrectFilePath, expectedDirectoryPath));
    }

    @Test
    void givenCorrectFilePath_WhenRepoExistButFileDoNotExist_ThenFileNotFoundExceptionOccursWhileTryingToOpenFile() {
        deleteFile(expectedAppPath);
        assertThrows(RuntimeException.class, () -> DeviceSetup.downloadAppAndGetFilePath(expectedAppPath, expectedDirectoryPath));
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
