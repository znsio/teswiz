package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.selenium.Configuration;
import com.applitools.eyes.visualgrid.model.DeviceName;
import com.applitools.eyes.visualgrid.model.ScreenOrientation;
import com.znsio.teswiz.visual.PlaywrightUfgTargetMapper;
import com.znsio.teswiz.visual.PlaywrightVisualSessionRequest;

class PlaywrightUfgTargetMapperTest {

    @Test
    void shouldMapDesktopAndDeviceUfgTargetsForPlaywright() {
        Configuration ufgConfig = new Configuration();
        ufgConfig.addBrowser(1920, 1024, BrowserType.CHROME);
        ufgConfig.addBrowser(1600, 1024, BrowserType.FIREFOX);
        ufgConfig.addDeviceEmulation(DeviceName.iPhone_15_Pro, ScreenOrientation.PORTRAIT);
        ufgConfig.addDeviceEmulation(DeviceName.OnePlus_7T_Pro, ScreenOrientation.LANDSCAPE);

        List<PlaywrightVisualSessionRequest.UfgTarget> targets = new PlaywrightUfgTargetMapper()
                .map(ufgConfig.getBrowsersInfo());

        assertThat(targets).hasSize(4);
        assertThat(targets).anySatisfy(target -> {
            assertThat(target.browserType()).isEqualTo("CHROME");
            assertThat(target.width()).isEqualTo(1920);
            assertThat(target.height()).isEqualTo(1024);
        });
        assertThat(targets).anySatisfy(target -> {
            assertThat(target.browserType()).isEqualTo("FIREFOX");
            assertThat(target.width()).isEqualTo(1600);
            assertThat(target.height()).isEqualTo(1024);
        });
        assertThat(targets).anySatisfy(target -> {
            assertThat(target.deviceName()).isEqualTo("iPhone 15 Pro");
            assertThat(target.screenOrientation()).isEqualTo("PORTRAIT");
        });
        assertThat(targets).anySatisfy(target -> {
            assertThat(target.deviceName()).isEqualTo("OnePlus 7T Pro");
            assertThat(target.screenOrientation()).isEqualTo("LANDSCAPE");
        });
    }
}
