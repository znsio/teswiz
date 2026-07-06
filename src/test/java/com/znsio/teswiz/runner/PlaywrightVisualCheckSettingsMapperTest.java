package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.Region;
import com.applitools.eyes.images.ImagesCheckSettings;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import com.applitools.eyes.selenium.fluent.Target;
import com.znsio.teswiz.exceptions.VisualTestSetupException;
import com.znsio.teswiz.visual.PlaywrightVisualCheckSettingsMapper;

class PlaywrightVisualCheckSettingsMapperTest {

    @Test
    void shouldMapSelectorBasedLayoutRegionsToImageCheckSettings() {
        SeleniumCheckSettings seleniumCheckSettings = Target.window()
                .layout(By.id("enterPassword"))
                .layout(By.id("name"))
                .matchLevel(MatchLevel.LAYOUT);
        PlaywrightVisualCheckSettingsMapper mapper = new PlaywrightVisualCheckSettingsMapper(by -> {
            if (by.equals(By.id("enterPassword"))) {
                return new Region(10, 20, 100, 30);
            }
            if (by.equals(By.id("name"))) {
                return new Region(10, 60, 180, 30);
            }
            throw new IllegalArgumentException("Unexpected locator: " + by);
        });

        ImagesCheckSettings imageCheckSettings = mapper.toImageCheckSettings(
                seleniumCheckSettings,
                new BufferedImage(400, 200, BufferedImage.TYPE_INT_ARGB));

        assertThat(imageCheckSettings.getMatchLevel()).isEqualTo(MatchLevel.LAYOUT);
        assertThat(imageCheckSettings.getLayoutRegions()).hasSize(2);
    }

    @Test
    void shouldFailFastForTargetLocatorRegionsThatNeedCropping() {
        SeleniumCheckSettings seleniumCheckSettings = Target.region(By.id("content"));
        PlaywrightVisualCheckSettingsMapper mapper = new PlaywrightVisualCheckSettingsMapper(by -> new Region(0, 0, 10, 10));

        assertThatThrownBy(() -> mapper.toImageCheckSettings(
                seleniumCheckSettings,
                new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB)))
                .isInstanceOf(VisualTestSetupException.class)
                .hasMessageContaining("Target.region")
                .hasMessageContaining("playwright-ts");
    }
}
