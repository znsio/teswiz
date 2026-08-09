package com.znsio.teswiz.visual;

import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;

public interface PlaywrightVisualDriver {
    void openVisualSession(PlaywrightVisualSessionRequest request);

    void checkWindow(String tag);

    void check(String tag, SeleniumCheckSettings checkSettings);

    void checkWindow(String tag, MatchLevel matchLevel);

    PlaywrightVisualResults closeVisualSession();

    boolean isVisualSessionDisabled();
}
