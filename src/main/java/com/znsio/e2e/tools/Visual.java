package com.znsio.e2e.tools;

import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import com.applitools.eyes.selenium.fluent.Target;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.epam.reportportal.service.ReportPortal;
import com.znsio.e2e.entities.TEST_CONTEXT;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class Visual {
    private final String visualTestNotEnabledMessage = "Visual Test is not enabled";
    private final Eyes eyes;
    private final TestExecutionContext context;
    private final ScreenShotManager screenShotManager;

    public Visual (Eyes eyes) {
        this.eyes = eyes;
        this.context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        this.screenShotManager = (ScreenShotManager) context.getTestState(TEST_CONTEXT.SCREENSHOT_MANAGER);
    }

    public Visual () {
        this.eyes = null;
        this.context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        this.screenShotManager = (ScreenShotManager) context.getTestState(TEST_CONTEXT.SCREENSHOT_MANAGER);
    }

    public Visual checkWindow (String fromScreen, String tag) {
        if (null != eyes) {
            eyes.check(getFormattedTagName(fromScreen, tag), Target.window());
        }
        screenShotManager.takeScreenShot(getFormattedTagName(fromScreen, tag));
        return this;
    }

    public Visual check (String fromScreen, String tag, SeleniumCheckSettings checkSettings) {
        if (null != eyes) {
            eyes.check(getFormattedTagName(fromScreen, tag), checkSettings);
        }
        screenShotManager.takeScreenShot(getFormattedTagName(fromScreen, tag));
        return this;
    }

    @NotNull
    private String getFormattedTagName (String fromScreen, String tag) {
        return fromScreen + " : " + tag;
    }

    public Visual checkWindow (String fromScreen, String tag, MatchLevel level) {
        if (null != eyes) {
            eyes.check(getFormattedTagName(fromScreen, tag), Target.window().matchLevel(level));
        }
        screenShotManager.takeScreenShot(getFormattedTagName(fromScreen, tag));
        return this;
    }

    public Visual takeScreenshot (String fromScreen, String tag) {
        screenShotManager.takeScreenShot(getFormattedTagName(fromScreen, tag));
        return this;
    }

    public String handleTestResults (String userPersona) {
        if (null == eyes) {
            String message = "Eyes is null. Visual Testing was skipped";
            System.out.println(message);
            return message;
        } else {
            TestResults visualResults = eyes.close(false);
            String reportUrl = handleTestResults(visualResults);
            String message = String.format("Visual Testing Results for user persona: '%s' :: '%s'", userPersona, reportUrl);
            System.out.println(message);
            ReportPortal.emitLog(message, "DEBUG", new Date());
            return reportUrl;
        }
    }

    private String handleTestResults (TestResults result) {
        System.out.println("\t\t" + result);
        System.out.printf("\t\tmatched = %d, mismatched = %d, missing = %d, isNew: %s, isPassed: %s%n",
                result.getMatches(),
                result.getMismatches(),
                result.getMissing(),
                result.isNew(),
                result.isPassed());
//        System.out.printf("\t\tName = '%s', \nDevice = %s,OS = %s, viewport = %dx%d, matched = %d, mismatched = %d, missing = %d, aborted = %s%n",
//                result.getName(),
//                result.getHostApp(),
//                result.getHostOS(),
//                result.getHostDisplaySize().getWidth(),
//                result.getHostDisplaySize().getHeight(),
//                result.getMatches(),
//                result.getMismatches(),
//                result.getMissing(),
//                (result.isAborted() ? "aborted" : "no"),
//                result.getAccessibilityStatus(),
//                result.getDuration());
        System.out.println("Visual Testing results available here: " + result.getUrl());
//        boolean hasMismatches = result.getMismatches() != 0 || result.isAborted();
        boolean hasMismatches = result.getMismatches() != 0;
        System.out.println("Visual testing differences found? - " + hasMismatches);
        return result.getUrl();
    }

}
