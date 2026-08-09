package com.znsio.teswiz.web.playwright.screen;

import com.znsio.teswiz.screen.theapp.AppLaunchScreen;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaywrightTsScreenModuleResolverTest {
    @Test
    void shouldPreferTestResourcesScreenRoot() {
        PlaywrightTsScreenModuleResolver resolver = new PlaywrightTsScreenModuleResolver(List.of(
                Path.of("src", "test", "resources", "playwright", "screens").toAbsolutePath(),
                Path.of("playwright", "screens").toAbsolutePath()));

        assertThat(resolver.findModulePath(AppLaunchScreen.class))
                .contains("theapp/app-launch.screen.ts");
    }
}
