package com.znsio.teswiz.runner;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.Region;
import com.applitools.eyes.fluent.GetRegion;
import com.applitools.eyes.images.ImagesCheckSettings;
import com.applitools.eyes.selenium.ElementSelector;
import com.applitools.eyes.selenium.PathNodeValue;
import com.applitools.eyes.selenium.TargetPathLocator;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import com.applitools.eyes.selenium.fluent.SimpleRegionBySelector;
import com.applitools.eyes.selenium.fluent.SimpleRegionByTargetPathLocator;
import com.znsio.teswiz.exceptions.VisualTestSetupException;

final class PlaywrightVisualCheckSettingsMapper {
    @FunctionalInterface
    interface RegionBoundsProvider {
        Region resolve(By by);
    }

    private final RegionBoundsProvider regionBoundsProvider;

    PlaywrightVisualCheckSettingsMapper(RegionBoundsProvider regionBoundsProvider) {
        this.regionBoundsProvider = regionBoundsProvider;
    }

    ImagesCheckSettings toImageCheckSettings(SeleniumCheckSettings seleniumCheckSettings, BufferedImage screenshot) {
        if (null != seleniumCheckSettings.getTargetRegion()) {
            throw unsupported("Target.region(Region)");
        }
        if (null != seleniumCheckSettings.getTargetPathLocator()) {
            throw unsupported("Target.region(By/WebElement)");
        }
        if (!seleniumCheckSettings.getFrameChain().isEmpty()) {
            throw unsupported("frame-based visual checks");
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

        ImagesCheckSettings imageCheckSettings = (ImagesCheckSettings) com.applitools.eyes.images.Target.image(screenshot);
        if (null != seleniumCheckSettings.getMatchLevel()) {
            imageCheckSettings = (ImagesCheckSettings) imageCheckSettings.matchLevel(seleniumCheckSettings.getMatchLevel());
        }
        if (null != seleniumCheckSettings.getIgnoreCaret()) {
            imageCheckSettings = (ImagesCheckSettings) imageCheckSettings.ignoreCaret(seleniumCheckSettings.getIgnoreCaret());
        }
        if (null != seleniumCheckSettings.getName()) {
            imageCheckSettings = (ImagesCheckSettings) imageCheckSettings.withName(seleniumCheckSettings.getName());
        }
        if (null != seleniumCheckSettings.getTimeout()) {
            imageCheckSettings = (ImagesCheckSettings) imageCheckSettings.timeout(seleniumCheckSettings.getTimeout());
        }
        imageCheckSettings = applyRegions(imageCheckSettings, seleniumCheckSettings.getIgnoreRegions(), RegionKind.IGNORE);
        imageCheckSettings = applyRegions(imageCheckSettings, seleniumCheckSettings.getLayoutRegions(), RegionKind.LAYOUT);
        imageCheckSettings = applyRegions(imageCheckSettings, seleniumCheckSettings.getStrictRegions(), RegionKind.STRICT);
        imageCheckSettings = applyRegions(imageCheckSettings, seleniumCheckSettings.getContentRegions(), RegionKind.CONTENT);
        if (Boolean.TRUE.equals(seleniumCheckSettings.getStitchContent())) {
            imageCheckSettings = (ImagesCheckSettings) imageCheckSettings.fully(true);
        }
        return imageCheckSettings;
    }

    private ImagesCheckSettings applyRegions(ImagesCheckSettings imageCheckSettings,
            GetRegion[] getRegions,
            RegionKind regionKind) {
        List<Region> resolvedRegions = new ArrayList<>();
        for (GetRegion getRegion : getRegions) {
            resolvedRegions.add(resolveRegion(getRegion));
        }
        if (resolvedRegions.isEmpty()) {
            return imageCheckSettings;
        }
        Region[] regionArray = resolvedRegions.toArray(new Region[0]);
        return switch (regionKind) {
            case IGNORE -> (ImagesCheckSettings) imageCheckSettings.ignore(regionArray);
            case LAYOUT -> (ImagesCheckSettings) imageCheckSettings.layout(regionArray);
            case STRICT -> (ImagesCheckSettings) imageCheckSettings.strict(regionArray);
            case CONTENT -> (ImagesCheckSettings) imageCheckSettings.content(regionArray);
        };
    }

    private Region resolveRegion(GetRegion getRegion) {
        if (getRegion instanceof SimpleRegionBySelector simpleRegionBySelector) {
            return regionBoundsProvider.resolve(simpleRegionBySelector.getSelector());
        }
        if (getRegion instanceof SimpleRegionByTargetPathLocator simpleRegion) {
            return resolveTargetPathLocator(simpleRegion.getTargetPathLocator());
        }
        throw new VisualTestSetupException(
                "Visual validation for WEB_ENGINE=playwright-ts does not yet support Applitools region type: '"
                        + getRegion.getClass().getSimpleName() + "'");
    }

    private Region resolveTargetPathLocator(TargetPathLocator targetPathLocator) {
        if (null == targetPathLocator || null != targetPathLocator.getParent()) {
            throw unsupported("nested target path locators");
        }
        PathNodeValue value = targetPathLocator.getValue();
        if (!(value instanceof ElementSelector elementSelector)) {
            throw unsupported("target path locators without element selectors");
        }
        return regionBoundsProvider.resolve(toBy(elementSelector));
    }

    private By toBy(ElementSelector elementSelector) {
        String type = elementSelector.getType();
        String selector = elementSelector.getSelector();
        return switch (type) {
            case "css selector" -> By.cssSelector(selector);
            case "xpath" -> By.xpath(selector);
            case "link text" -> By.linkText(selector);
            case "partial link text" -> By.partialLinkText(selector);
            default -> throw new VisualTestSetupException(
                    "Visual validation for WEB_ENGINE=playwright-ts does not yet support Applitools selector type: '"
                            + type + "'");
        };
    }

    private VisualTestSetupException unsupported(String capability) {
        return new VisualTestSetupException(
                "Visual validation for WEB_ENGINE=playwright-ts does not yet support " + capability
                        + ". Use Target.window() based checks for Playwright in the current phase.");
    }

    private enum RegionKind {
        IGNORE,
        LAYOUT,
        STRICT,
        CONTENT
    }
}
