package com.znsio.teswiz.config.app;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;

public final class AppVersionDetector {
    private static final Pattern ANDROID_VERSION_PATTERN = Pattern.compile("versionName='(\\d+(\\.\\d+)+)'",
            Pattern.MULTILINE);
    private static final Pattern WINDOWS_VERSION_PATTERN = Pattern.compile("Version=(\\d+(\\.\\d+)+)",
            Pattern.MULTILINE);

    private final CommandExecutor commandExecutor;

    public AppVersionDetector() {
        this(CommandLineExecutor::execCommand);
    }

    AppVersionDetector(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public Optional<String> detectAndroidAppVersion(String appPath, String androidHomePath, boolean isWindows)
            throws IOException {
        File appFile = new File(appPath);
        String appFilePath = appFile.getCanonicalPath();
        String searchPattern = isWindows ? "findstr" : "grep";
        File buildToolsFolder = new File(androidHomePath, "build-tools");
        File buildVersionFolder = Objects.requireNonNull(buildToolsFolder.listFiles())[0];
        File aaptExecutable = new File(buildVersionFolder, "aapt").getAbsoluteFile();
        String[] commandToGetAppVersion = new String[]{aaptExecutable.toString(), "dump",
                "badging", appFilePath, "|", searchPattern, "versionName"};
        return detectVersion(commandToGetAppVersion, ANDROID_VERSION_PATTERN);
    }

    public Optional<String> detectWindowsAppVersion(String appPath) throws IOException {
        File appFile = new File(appPath);
        String nameVariable = "name=\"" + appFile.getCanonicalPath().replace("\\", "\\\\") + "\"";
        String[] commandToGetAppVersion = new String[]{"wmic", "datafile", "where", nameVariable, "get", "Version",
                "/value"};
        return detectVersion(commandToGetAppVersion, WINDOWS_VERSION_PATTERN);
    }

    static Optional<String> extractVersion(String commandOutput, Pattern pattern) {
        if (null == commandOutput || commandOutput.isEmpty()) {
            return Optional.empty();
        }
        Matcher matcher = pattern.matcher(commandOutput);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    private Optional<String> detectVersion(String[] commandToGetAppVersion, Pattern pattern) {
        CommandLineResponse commandResponse = commandExecutor.exec(commandToGetAppVersion);
        return extractVersion(commandResponse.getStdOut(), pattern);
    }

    @FunctionalInterface
    interface CommandExecutor {
        CommandLineResponse exec(String[] command);
    }
}
