package com.znsio.teswiz.config.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.znsio.teswiz.tools.cmd.CommandLineResponse;

class AppVersionDetectorTest {
    @Test
    void shouldExtractAndroidVersionFromCommandOutput() {
        Optional<String> version = AppVersionDetector.extractVersion(
                "package: name='com.sample' versionCode='12' versionName='4.5.6'",
                Pattern.compile("versionName='(\\d+(\\.\\d+)+)'", Pattern.MULTILINE));

        assertThat(version).contains("4.5.6");
    }

    @Test
    void shouldReturnEmptyWhenVersionCannotBeExtracted() {
        Optional<String> version = AppVersionDetector.extractVersion(
                "package: name='com.sample' versionCode='12'",
                Pattern.compile("versionName='(\\d+(\\.\\d+)+)'", Pattern.MULTILINE));

        assertThat(version).isEmpty();
    }

    @Test
    void shouldBuildAndroidVersionCommandUsingUnixSearchTool() throws IOException {
        AtomicReference<String[]> executedCommand = new AtomicReference<>();
        AppVersionDetector detector = new AppVersionDetector(command -> {
            executedCommand.set(command);
            CommandLineResponse response = new CommandLineResponse();
            response.setStdOut("package: versionName='1.2.3'");
            return response;
        });
        Path androidHome = Files.createTempDirectory("android-home");
        Path buildToolsVersion = Files.createDirectories(androidHome.resolve("build-tools").resolve("34.0.0"));
        Files.createFile(buildToolsVersion.resolve("aapt"));
        Path appFile = Files.createTempFile("sample-app", ".apk");

        Optional<String> version = detector.detectAndroidAppVersion(appFile.toString(), androidHome.toString(), false);

        assertThat(version).contains("1.2.3");
        assertThat(executedCommand.get()).containsExactly(
                buildToolsVersion.resolve("aapt").toFile().getAbsolutePath(),
                "dump",
                "badging",
                appFile.toFile().getCanonicalPath(),
                "|",
                "grep",
                "versionName");
    }

    @Test
    void shouldBuildWindowsVersionCommandAndParseVersion() throws IOException {
        AtomicReference<String[]> executedCommand = new AtomicReference<>();
        AppVersionDetector detector = new AppVersionDetector(command -> {
            executedCommand.set(command);
            CommandLineResponse response = new CommandLineResponse();
            response.setStdOut("Version=7.8.9");
            return response;
        });
        Path appFile = Files.createTempFile("sample-app", ".exe");

        Optional<String> version = detector.detectWindowsAppVersion(appFile.toString());

        assertThat(version).contains("7.8.9");
        assertThat(executedCommand.get()[0]).isEqualTo("wmic");
        assertThat(executedCommand.get()[1]).isEqualTo("datafile");
        assertThat(executedCommand.get()[2]).isEqualTo("where");
        assertThat(executedCommand.get()[4]).isEqualTo("get");
        assertThat(executedCommand.get()[5]).isEqualTo("Version");
        assertThat(executedCommand.get()[6]).isEqualTo("/value");
    }
}
