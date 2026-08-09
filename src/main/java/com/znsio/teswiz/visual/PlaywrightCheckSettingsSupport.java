package com.znsio.teswiz.visual;

import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import com.znsio.teswiz.exceptions.VisualTestSetupException;

public final class PlaywrightCheckSettingsSupport {
    private static final String PLAYWRIGHT_ENGINE_HINT =
            "WEB_ENGINE=playwright-java or WEB_ENGINE=playwright-ts";

    public PlaywrightCheckOptions toCheckOptions(SeleniumCheckSettings seleniumCheckSettings) {
        if (!seleniumCheckSettings.getFrameChain().isEmpty()) {
            throw unsupported("frame-based visual checks");
        }
        if (null != seleniumCheckSettings.getTargetRegion()) {
            throw unsupported("region-targeted visual checks");
        }
        if (null != seleniumCheckSettings.getTargetPathLocator()) {
            throw unsupported("selector-targeted visual checks");
        }
        if (seleniumCheckSettings.getIgnoreRegions().length > 0) {
            throw unsupported("ignore regions");
        }
        if (seleniumCheckSettings.getLayoutRegions().length > 0) {
            throw unsupported("layout regions");
        }
        if (seleniumCheckSettings.getStrictRegions().length > 0) {
            throw unsupported("strict regions");
        }
        if (seleniumCheckSettings.getContentRegions().length > 0) {
            throw unsupported("content regions");
        }
        if (seleniumCheckSettings.getFloatingRegions().length > 0) {
            throw unsupported("floating regions");
        }
        if (seleniumCheckSettings.getAccessibilityRegions().length > 0) {
            throw unsupported("accessibility regions");
        }
        if (seleniumCheckSettings.getDynamicRegions().length > 0) {
            throw unsupported("dynamic regions");
        }
        return new PlaywrightCheckOptions(Boolean.TRUE.equals(seleniumCheckSettings.getStitchContent()),
                seleniumCheckSettings.getMatchLevel());
    }

    private VisualTestSetupException unsupported(String capability) {
        return new VisualTestSetupException(
                "Visual validation for " + PLAYWRIGHT_ENGINE_HINT + " does not yet support " + capability
                        + ". Use Target.window() based checks for Playwright in the current phase.");
    }

    public record PlaywrightCheckOptions(boolean fully, MatchLevel matchLevel) {
    }
}
