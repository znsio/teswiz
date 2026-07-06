package com.znsio.teswiz.runner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.tools.ReportPortalLogger;

public final class PlaywrightBrowserConfigMigrationReporter {
    private static final Logger LOGGER = LogManager.getLogger(PlaywrightBrowserConfigMigrationReporter.class.getName());
    private static final Map<String, Notice> NOTICES = new LinkedHashMap<>();

    private PlaywrightBrowserConfigMigrationReporter() {
    }

    public static synchronized Path registerMigrationArtifact(TestExecutionContext context, String sourceConfigPath,
            JSONObject originalConfig, PlaywrightBrowserConfigMigrator migrator) {
        PlaywrightBrowserConfigMigrator.MigrationResult migrationResult = migrator.migrate(originalConfig);
        if (null == migrationResult.migratedConfig()) {
            return null;
        }

        Path reportsDir = resolveReportsDirectory(context);
        String generatedFileName = buildGeneratedFileName(sourceConfigPath);
        Path generatedFile = reportsDir.resolve(generatedFileName);
        try {
            Files.createDirectories(reportsDir);
            Files.writeString(generatedFile, migrationResult.migratedConfig().toString(2), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write Playwright browser config migration artifact: " + generatedFile,
                    e);
        }

        registerNotice(sourceConfigPath, generatedFile.toString(), migrationResult.warnings());
        return generatedFile;
    }

    public static synchronized void registerNotice(String sourceConfigPath, String generatedConfigPath, List<String> warnings) {
        String key = sourceConfigPath + "->" + generatedConfigPath;
        NOTICES.putIfAbsent(key, new Notice(sourceConfigPath, generatedConfigPath, new ArrayList<>(warnings)));
    }

    public static synchronized String buildSummaryMessage() {
        if (NOTICES.isEmpty()) {
            return "";
        }

        StringBuilder message = new StringBuilder();
        message.append("\n================ Playwright Browser Config Migration ================\n");
        for (Notice notice : NOTICES.values()) {
            message.append("A Playwright-ready browser config was generated.\n");
            message.append("  Source config: ").append(notice.sourceConfigPath()).append('\n');
            message.append("  Generated config: ").append(notice.generatedConfigPath()).append('\n');
            message.append("  Your current config is still supported.\n");
            message.append("  To adopt the new style, review the generated file and replace the old config when ready.\n");
            if (!notice.warnings().isEmpty()) {
                message.append("  Review notes:\n");
                notice.warnings().forEach(warning -> message.append("    - ").append(warning).append('\n'));
            }
        }
        message.append("===================================================================\n");
        return message.toString();
    }

    public static synchronized void emitSummaryIfPresent() {
        String message = buildSummaryMessage();
        if (message.isBlank()) {
            return;
        }

        LOGGER.warn(message);
        System.out.println(message);
        ReportPortalLogger.logWarningMessage(message);
    }

    public static synchronized void clear() {
        NOTICES.clear();
    }

    private static Path resolveReportsDirectory(TestExecutionContext context) {
        String scenarioLogDirectory = context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        if (null != scenarioLogDirectory && !scenarioLogDirectory.isBlank()) {
            Path scenarioPath = Path.of(scenarioLogDirectory);
            Path reportsDir = scenarioPath.getParent();
            if (null != reportsDir) {
                return reportsDir;
            }
        }
        return Path.of(System.getProperty("user.dir"), FileLocations.REPORTS_DIRECTORY);
    }

    private static String buildGeneratedFileName(String sourceConfigPath) {
        String fileName = Path.of(sourceConfigPath).getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        String baseName = extensionIndex > 0 ? fileName.substring(0, extensionIndex) : fileName;
        return baseName + "-playwright-recommended.json";
    }

    private record Notice(String sourceConfigPath, String generatedConfigPath, List<String> warnings) {
    }
}
