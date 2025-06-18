package com.znsio.teswiz.tools;

import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.runner.Runner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

class FileUtilsTest {
    private static final Logger LOGGER = LogManager.getLogger(FileUtilsTest.class.getName());

    @TempDir
    Path tempDir;

    private String tempDirPath;

    @BeforeEach
    void setUp() {
        tempDirPath = tempDir.toAbsolutePath().toString();
    }

    @Test
    void createDirectory_withValidPath_shouldCreateDirectory() {
        String newDir = tempDir.resolve("newDir").toString();
        assertThat(FileUtils.createDirectory(newDir)).isTrue();
        assertThat(newDir).isNotBlank();
        assertThat(newDir).endsWith("newDir");
    }

    @Test
    void createDirectory_whenAlreadyExists_shouldReturnTrue() {
        String dir = tempDir.toString();
        assertThat(FileUtils.createDirectory(dir)).isTrue();
    }

    @Test
    void createDirectory_withNull_shouldThrowException() {
        assertThatThrownBy(() -> FileUtils.createDirectory(null))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Directory is null or empty");
    }

    @Test
    void createDirectory_withEmptyString_shouldThrowException() {
        assertThatThrownBy(() -> FileUtils.createDirectory(""))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Directory is null or empty");
    }

    @Test
    void createParentDirectory_withValidInputs_shouldCreateDirectory() {
        boolean result = FileUtils.createParentDirectory(tempDirPath, "childDir");
        assertThat(result).isTrue();
        assertThat(tempDir.resolve("childDir")).exists().isDirectory();
    }

    @Test
    void createParentDirectory_whenParentAndChildExist_shouldReturnTrue() {
        String childDir = "existing";
        Path dir = tempDir.resolve(childDir);
        dir.toFile().mkdirs();
        assertThat(FileUtils.createParentDirectory(tempDirPath, childDir)).isTrue();
    }

    @Test
    void createParentDirectory_withNullParent_shouldThrowException() {
        assertThatThrownBy(() -> FileUtils.createParentDirectory(null, "child"))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Parent directory is null or empty");
    }

    @Test
    void createParentDirectory_withNullChild_shouldThrowException() {
        assertThatThrownBy(() -> FileUtils.createParentDirectory("parent", null))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Child directory is null or empty");
    }

    @Test
    void createParentDirectory_withNullParentAndChild_shouldThrowException() {
        assertThatThrownBy(() -> FileUtils.createParentDirectory(null, null))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Parent directory is null or empty");
    }

    @Test
    void createParentDirectory_withEmptyChild_shouldThrowException() {
        assertThatThrownBy(() -> FileUtils.createParentDirectory(tempDirPath, ""))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Child directory is null or empty");
    }

    @Test
    void createParentDirectory_withEmptyParent_shouldThrowException() {
        assertThatThrownBy(() -> FileUtils.createParentDirectory("", "tempDirPath"))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Parent directory is null or empty");
    }

    @Test
    void createParentDirectory_withEmptyParentAndChild_shouldThrowException() {
        assertThatThrownBy(() -> FileUtils.createParentDirectory("", ""))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Parent directory is null or empty");
    }

    @Test
    void createParentDirectory_withPathTraversal_shouldCreateExpectedDir() {
        String child = "a/../b";
        assertThat(FileUtils.createParentDirectory(tempDirPath, child)).isTrue();
        assertThat(tempDir.resolve("b")).exists().isDirectory();
    }

    @Test
    void createDirectory_withSpecialCharacters_shouldSucceed() {
        String dirName = "weird-~!@#$%^&()_+{}[]";
        String path = tempDir.resolve(dirName).toString();
        assertThat(FileUtils.createDirectory(path)).isTrue();
        assertThat(new java.io.File(path)).exists().isDirectory();
    }

    @Test
    void createDirectory_shouldBeIdempotent() {
        String path = tempDir.resolve("idempotent").toString();
        assertThat(FileUtils.createDirectory(path)).isTrue();
        assertThat(FileUtils.createDirectory(path)).isTrue();
    }


//    private static final String LOG_DIR = "./target/testLogs";
//    private static Path tempDir;

//    @BeforeAll
//    static void setupOnce() throws Exception {
//        tempDir = Files.createTempDirectory("fileutils-test");
//    }
//
//    @AfterAll
//    static void cleanupOnce() throws Exception {
//        // Delete tempDir recursively
//        deleteRecursively(tempDir.toFile());
//    }
//
//    private static void deleteRecursively(File file) {
//        if (file.isDirectory()) {
//            File[] children = file.listFiles();
//            if (children != null) {
//                for (File child : children) {
//                    deleteRecursively(child);
//                }
//            }
//        }
//
//        if (!file.delete()) {
//            System.err.printf("⚠️ Failed to delete: %s%n", file.getAbsolutePath());
//        }
//    }
//
//    @Test
//    void shouldCreateNewDirectory() {
//        String dirName = tempDir.resolve("new-dir").toString();
//        boolean result = FileUtils.createDirectory(dirName);
//        assertThat(result).isTrue();
//        assertThat(new File(dirName)).exists().isDirectory();
//    }
//
//    @Test
//    void shouldReturnTrueForAlreadyExistingDirectory() {
//        String dirName = tempDir.resolve("existing-dir").toString();
//        new File(dirName).mkdirs(); // create manually
//        boolean result = FileUtils.createDirectory(dirName);
//        assertThat(result).isTrue();
//    }
//
//    @Test
//    void shouldThrowExceptionForNullDirectory() {
//        assertThatThrownBy(() -> FileUtils.createDirectory(null))
//                .isInstanceOf(InvalidTestDataException.class)
//                .hasMessageContaining("Directory is null or empty");
//    }
//
//    @Test
//    void shouldThrowExceptionForEmptyDirectory() {
//        assertThatThrownBy(() -> FileUtils.createDirectory("   "))
//                .isInstanceOf(InvalidTestDataException.class)
//                .hasMessageContaining("Directory is null or empty");
//    }
//
//    @Test
//    void shouldCreateParentAndChildDirectory() {
//        String parent = tempDir.resolve("parent").toString();
//        String child = "child";
//        boolean result = FileUtils.createParentDirectory(parent, child);
//        File combined = new File(parent, child);
//        assertThat(result).isTrue();
//        assertThat(combined).exists().isDirectory();
//    }
//
//    @Test
//    void shouldThrowExceptionWhenParentIsNull() {
//        assertThatThrownBy(() -> FileUtils.createParentDirectory(null, "child"))
//                .isInstanceOf(InvalidTestDataException.class)
//                .hasMessageContaining("Parent directory is null or empty");
//    }
//
//    @Test
//    void shouldThrowExceptionWhenChildIsEmpty() {
//        assertThatThrownBy(() -> FileUtils.createParentDirectory("/tmp", " "))
//                .isInstanceOf(InvalidTestDataException.class)
//                .hasMessageContaining("Child directory is null or empty");
//    }
//
//    @Test
//    void createParentDirectory_createsOnlyChildIfParentExists(@TempDir Path parent) {
//        String child = "subdir";
//        assertThat(FileUtils.createParentDirectory(parent.toString(), child)).isTrue();
//        assertThat(parent.resolve(child)).exists().isDirectory();
//    }







//
//    @Test
//    void nullDirectoryNameTest() {
//        assertThatThrownBy(() -> FileUtils.createDirectory(null))
//                .as("Should throw InvalidTestDataException for null directory path")
//                .isInstanceOf(InvalidTestDataException.class)
//                .hasMessageContaining("Directory is null or empty"); // adjust the message as per actual exception
//    }
//
//    @Test
//    void nullSystemPropertyTest() {
//        assertThatThrownBy(() -> FileUtils.createDirectory(System.getProperty(Runner.USER_DIRECTORY)))
//                .as("Should throw InvalidTestDataException for invalid system property")
//                .isInstanceOf(InvalidTestDataException.class)
//                .hasMessageContaining("Directory is null or empty"); // adjust the message as per actual exception
//    }
//
//    @Test
//    void emptyDirectoryNameTest() {
//        assertThatThrownBy(() -> FileUtils.createDirectory(""))
//                .as("Should throw InvalidTestDataException for empty directory path")
//                .isInstanceOf(InvalidTestDataException.class)
//                .hasMessageContaining("Directory is null or empty"); // adjust the message as per actual exception
//    }
//
//    @Test
//    void existingDirectoryTest() {
//        assertThat(FileUtils.createDirectory(Runner.USER_DIRECTORY)).as("Existing user directory").isTrue();
//    }
//
//    @Test
//    void createDirectoryTest() {
//        String randomizedString = Randomizer.randomizeString(10);
//        assertThat(FileUtils.createDirectory(LOG_DIR + File.separator + randomizedString + File.separator + randomizedString + ".foo")).as("Parent directory created").isTrue();
//    }
//
//    @Test
//    void createParentDirectory() {
//        assertThat(FileUtils.createDirectory(System.getProperty(Runner.USER_DIRECTORY))).as("Directory exists").isTrue();
//    }
//
//    @Test
//    void createDirectory() {
//    }
}
