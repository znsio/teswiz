package com.znsio.teswiz.visual;

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

public final class PlaywrightVisualCheckSettingsMapper {
    @FunctionalInterface
    public interface RegionBoundsProvider {
        Region resolve(By by);
    }

    private final RegionBoundsProvider regionBoundsProvider;

    public PlaywrightVisualCheckSettingsMapper(RegionBoundsProvider regionBoundsProvider) {
        this.regionBoundsProvider = regionBoundsProvider;
    }

    public ImagesCheckSettings toImageCheckSettings(SeleniumCheckSettings seleniumCheckSettings, BufferedImage screenshot) {
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

        Region cropRegion = resolveCropRegion(seleniumCheckSettings);
        BufferedImage targetImage = cropImage(screenshot, cropRegion);
        ImagesCheckSettings imageCheckSettings = (ImagesCheckSettings) com.applitools.eyes.images.Target.image(targetImage);
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
        imageCheckSettings = applyRegions(imageCheckSettings, seleniumCheckSettings.getIgnoreRegions(), RegionKind.IGNORE,
                cropRegion);
        imageCheckSettings = applyRegions(imageCheckSettings, seleniumCheckSettings.getLayoutRegions(), RegionKind.LAYOUT,
                cropRegion);
        imageCheckSettings = applyRegions(imageCheckSettings, seleniumCheckSettings.getStrictRegions(), RegionKind.STRICT,
                cropRegion);
        imageCheckSettings = applyRegions(imageCheckSettings, seleniumCheckSettings.getContentRegions(), RegionKind.CONTENT,
                cropRegion);
        if (Boolean.TRUE.equals(seleniumCheckSettings.getStitchContent())) {
            imageCheckSettings = (ImagesCheckSettings) imageCheckSettings.fully(true);
        }
        return imageCheckSettings;
    }

    private ImagesCheckSettings applyRegions(ImagesCheckSettings imageCheckSettings,
            GetRegion[] getRegions,
            RegionKind regionKind,
            Region cropRegion) {
        List<Region> resolvedRegions = new ArrayList<>();
        for (GetRegion getRegion : getRegions) {
            resolvedRegions.add(rebaseRegion(resolveRegion(getRegion), cropRegion));
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

    private Region resolveCropRegion(SeleniumCheckSettings seleniumCheckSettings) {
        if (null != seleniumCheckSettings.getTargetRegion()) {
            return seleniumCheckSettings.getTargetRegion();
        }
        if (null != seleniumCheckSettings.getTargetPathLocator()) {
            return resolveTargetPathLocator(seleniumCheckSettings.getTargetPathLocator());
        }
        return null;
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

    private BufferedImage cropImage(BufferedImage screenshot, Region cropRegion) {
        if (null == cropRegion) {
            return screenshot;
        }
        int x = clamp(cropRegion.getLeft(), 0, screenshot.getWidth());
        int y = clamp(cropRegion.getTop(), 0, screenshot.getHeight());
        int maxWidth = screenshot.getWidth() - x;
        int maxHeight = screenshot.getHeight() - y;
        int width = clamp(cropRegion.getWidth(), 0, maxWidth);
        int height = clamp(cropRegion.getHeight(), 0, maxHeight);
        if (width <= 0 || height <= 0) {
            throw new VisualTestSetupException("Visual validation for WEB_ENGINE=playwright-ts resolved an empty target region.");
        }
        return screenshot.getSubimage(x, y, width, height);
    }

    private Region rebaseRegion(Region region, Region cropRegion) {
        if (null == cropRegion) {
            return region;
        }
        return new Region(region.getLeft() - cropRegion.getLeft(), region.getTop() - cropRegion.getTop(),
                region.getWidth(), region.getHeight());
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
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
