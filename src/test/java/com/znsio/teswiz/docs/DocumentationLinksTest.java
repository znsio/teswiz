package com.znsio.teswiz.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class DocumentationLinksTest {
    private static final Path REPO_ROOT = Path.of(System.getProperty("user.dir"));
    private static final Set<String> EXCLUDED_DIR_SEGMENTS =
            Set.of("node_modules", "build", ".git", ".gradle", "temp", "target", "bin", "libs");
    private static final Pattern MARKDOWN_LINK = Pattern.compile("\\[[^]]*]\\(([^)]+)\\)");

    @Test
    void allLocalLinksInMarkdownFilesShouldResolve() throws IOException {
        List<String> violations = new ArrayList<>();

        for (Path markdownFile : findMarkdownFiles()) {
            List<String> lines = Files.readAllLines(markdownFile);
            for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
                checkLine(markdownFile, lineNumber + 1, lines.get(lineNumber), violations);
            }
        }

        assertThat(violations)
                .as("Found broken local links in markdown files:%n%s", String.join("\n", violations))
                .isEmpty();
    }

    private void checkLine(Path markdownFile, int lineNumber, String line, List<String> violations) {
        Matcher matcher = MARKDOWN_LINK.matcher(line);
        while (matcher.find()) {
            String url = matcher.group(1).trim();
            if (isExternalOrNonFileLink(url)) {
                continue;
            }
            if (url.startsWith("/")) {
                violations.add(formatViolation(markdownFile, lineNumber, url, "absolute path - use a relative path"));
                continue;
            }
            String pathPart = url.split("#", 2)[0];
            if (pathPart.isBlank()) {
                continue;
            }
            Path target = markdownFile.getParent().resolve(pathPart).normalize();
            if (!Files.exists(target)) {
                violations.add(formatViolation(markdownFile, lineNumber, url,
                        "target does not exist: " + target));
            }
        }
    }

    private boolean isExternalOrNonFileLink(String url) {
        return url.startsWith("http://") || url.startsWith("https://") || url.startsWith("mailto:");
    }

    private String formatViolation(Path markdownFile, int lineNumber, String url, String reason) {
        return String.format("%s:%d -> (%s) [%s]", REPO_ROOT.relativize(markdownFile), lineNumber, url, reason);
    }

    private List<Path> findMarkdownFiles() throws IOException {
        try (Stream<Path> paths = Files.walk(REPO_ROOT)) {
            return paths
                    .filter(path -> path.toString().endsWith(".md"))
                    .filter(this::isNotExcluded)
                    .toList();
        }
    }

    private boolean isNotExcluded(Path path) {
        Path relative = REPO_ROOT.relativize(path);
        for (Path segment : relative) {
            if (EXCLUDED_DIR_SEGMENTS.contains(segment.toString())) {
                return false;
            }
        }
        return true;
    }
}
