package com.znsio.teswiz.tools;

import com.znsio.teswiz.exceptions.InvalidTestDataException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileUtilsTest {
    private static final Logger LOGGER = LogManager.getLogger(FileUtilsTest.class.getName());

    private File tempDir;

    @BeforeAll
    void setup() throws IOException {
        tempDir = Files.createTempDirectory("fileutils-test-").toFile();
        LOGGER.info("Running FileUtilsTest with temp directory: {}", tempDir.getAbsolutePath());
        tempDir.deleteOnExit();
    }

    @Test
    void createDirectory_shouldCreateNewDirectory() {
        File dir = new File(tempDir, "newDir");
        File result = FileUtils.createDirectory(dir.getAbsolutePath());

        assertThat(result).exists().isDirectory();
    }

    @Test
    void createDirectory_shouldReturnExistingDirectory() {
        File dir = new File(tempDir, "existingDir");
        boolean created = dir.mkdirs();
        assertThat(created).isTrue();

        File result = FileUtils.createDirectory(dir.getAbsolutePath());
        assertThat(result).exists().isEqualTo(dir);
    }

    @Test
    void createDirectory_shouldThrowExceptionForNullPath() {
        assertThatThrownBy(() -> FileUtils.createDirectory(null)).isInstanceOf(InvalidTestDataException.class).hasMessageContaining("Directory is null or empty");
    }

    @Test
    void createDirectory_shouldThrowExceptionForEmptyPath() {
        assertThatThrownBy(() -> FileUtils.createDirectory("   ")).isInstanceOf(InvalidTestDataException.class).hasMessageContaining("Directory is null or empty");
    }

    @Test
    void createDirectory_shouldThrowIfPathIsAFile() throws IOException {
        File file = new File(tempDir, "someFile.txt");
        boolean created = file.createNewFile();
        assertThat(created).isTrue();

        assertThatThrownBy(() -> FileUtils.createDirectory(file.getAbsolutePath())).isInstanceOf(InvalidTestDataException.class).hasMessageContaining("Path exists but is a file");
    }

    @Test
    void createDirectoryIn_shouldCreateChildInExistingParent() {
        File child = FileUtils.createDirectoryIn(tempDir.getAbsolutePath(), "nested/child");

        assertThat(child).exists().isDirectory();
    }

    @Test
    void createDirectoryIn_shouldThrowIfParentIsInvalid() {
        assertThatThrownBy(() -> FileUtils.createDirectoryIn(null, "child")).isInstanceOf(InvalidTestDataException.class).hasMessageContaining("Parent is null or empty");
    }

    @Test
    void createDirectoryIn_shouldThrowIfChildIsInvalid() {
        assertThatThrownBy(() -> FileUtils.createDirectoryIn("validParent", " ")).isInstanceOf(InvalidTestDataException.class).hasMessageContaining("Child is null or empty");
    }

    @Test
    void copyFile_copiesContentSuccessfully() throws IOException {
        File src = new File(tempDir, "source.txt");
        File dest = new File(tempDir, "destination.txt");

        boolean fileCreated = src.createNewFile();
        assertThat(fileCreated).isTrue();
        Files.writeString(src.toPath(), "test content");

        FileUtils.copyFile(src, dest);

        assertThat(dest).exists();
        assertThat(Files.readAllBytes(dest.toPath())).as("Destination file content should match source").isEqualTo(Files.readAllBytes(src.toPath()));
    }

    @Test
    void copyFile_shouldLogWarningIfSourceNotFound() {
        File fakeSrc = new File(tempDir, "nonexistent.txt");
        File dest = new File(tempDir, "dummy.txt");

        FileUtils.copyFile(fakeSrc, dest);

        assertThat(dest).doesNotExist(); // file copy should silently fail
    }
}
