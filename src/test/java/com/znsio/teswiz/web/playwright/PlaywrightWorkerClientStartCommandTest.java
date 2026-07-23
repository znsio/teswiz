package com.znsio.teswiz.web.playwright;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class PlaywrightWorkerClientStartCommandTest {
    @Test
    void shouldLaunchWorkerWithTypeScriptSupportEnabled() {
        Path workerScriptPath = Path.of("playwright", "worker.mjs").toAbsolutePath();
        PlaywrightWorkerClient workerClient = new PlaywrightWorkerClient(workerScriptPath);

        List<String> startCommand = workerClient.buildStartCommand();

        assertThat(startCommand).containsExactly("node", "--experimental-strip-types", workerScriptPath.toString());
    }
}
