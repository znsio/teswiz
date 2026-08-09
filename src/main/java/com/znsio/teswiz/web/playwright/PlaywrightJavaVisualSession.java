package com.znsio.teswiz.web.playwright;

import java.util.Map;
import java.util.List;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.EyesRunner;
import com.applitools.eyes.FileLogger;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.ProxySettings;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.TestResultContainer;
import com.applitools.eyes.TestResultsSummary;
import com.applitools.eyes.config.Configuration;
import com.applitools.eyes.playwright.ClassicRunner;
import com.applitools.eyes.playwright.Eyes;
import com.applitools.eyes.playwright.fluent.PlaywrightCheckSettings;
import com.applitools.eyes.playwright.fluent.Target;
import com.applitools.eyes.playwright.visualgrid.VisualGridRunner;
import com.applitools.eyes.visualgrid.model.DeviceName;
import com.applitools.eyes.visualgrid.model.ScreenOrientation;
import com.applitools.eyes.visualgrid.services.RunnerOptions;
import com.microsoft.playwright.Page;
import com.znsio.teswiz.visual.PlaywrightCheckSettingsSupport;
import com.znsio.teswiz.visual.PlaywrightVisualSessionRequest;
import com.znsio.teswiz.visual.PlaywrightVisualResults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class PlaywrightJavaVisualSession {
    private static final Logger LOGGER = LogManager.getLogger(PlaywrightJavaVisualSession.class.getName());

    private final Page page;
    private final PlaywrightCheckSettingsSupport checkSettingsSupport = new PlaywrightCheckSettingsSupport();

    private EyesRunner runner;
    private Eyes eyes;

    PlaywrightJavaVisualSession(Page page) {
        this.page = page;
    }

    void open(PlaywrightVisualSessionRequest request) {
        runner = createRunner(request);
        runner.setDontCloseBatches(true);
        eyes = new Eyes(runner);
        Configuration configuration = new Configuration();
        configuration.setServerUrl(request.serverUrl());
        configuration.setApiKey(request.apiKey());
        configuration.setBranchName(request.branchName());
        configuration.setEnvironmentName(request.environmentName());
        configuration.setMatchLevel(request.defaultMatchLevel());
        configuration.setSaveNewTests(request.saveNewTests());
        setIfPresent(request.baselineEnvName(), configuration::setBaselineEnvName);
        setIfPresent(request.appName(), configuration::setAppName);
        if (request.useUfg()) {
            addUfgTargets(configuration, request.ufgTargets());
        }
        setBatch(configuration, request.batchMetadata());
        eyes.setConfiguration(configuration);
        eyes.setIsDisabled(!request.enabled());
        setIfPresent(request.proxyUrl(), proxyUrl -> eyes.setProxy(new ProxySettings(proxyUrl)));
        setIfPresent(request.logFilePath(), logFilePath -> eyes.setLogHandler(
                new FileLogger(logFilePath, true, request.verboseLogs())));
        addProperties(request.customProperties());
        eyes.open(page, request.appName(), request.testName(), request.viewportSize());
    }

    void checkWindow(String tag) {
        eyes.check(tag, Target.window());
    }

    void check(String tag, com.applitools.eyes.selenium.fluent.SeleniumCheckSettings checkSettings) {
        PlaywrightCheckSettingsSupport.PlaywrightCheckOptions checkOptions = checkSettingsSupport.toCheckOptions(
                checkSettings);
        PlaywrightCheckSettings target = Target.window();
        if (checkOptions.fully()) {
            target = target.fully();
        }
        if (null != checkOptions.matchLevel()) {
            target = target.matchLevel(checkOptions.matchLevel());
        }
        eyes.check(tag, target);
    }

    void checkWindow(String tag, MatchLevel matchLevel) {
        eyes.check(tag, Target.window().matchLevel(matchLevel));
    }

    PlaywrightVisualResults close() {
        if (runner instanceof VisualGridRunner visualGridRunner) {
            eyes.closeAsync();
            TestResultsSummary summary = visualGridRunner.getAllTestResults(false);
            List<PlaywrightVisualResults.Entry> entries = java.util.stream.StreamSupport.stream(summary.spliterator(), false)
                    .map(this::toEntry)
                    .toList();
            return new PlaywrightVisualResults(entries);
        }
        return PlaywrightVisualResults.single(eyes.close(false));
    }

    boolean isDisabled() {
        return null == eyes || Boolean.TRUE.equals(eyes.getIsDisabled());
    }

    private void addProperties(Map<String, String> customProperties) {
        for (Map.Entry<String, String> entry : customProperties.entrySet()) {
            eyes.addProperty(entry.getKey(), entry.getValue());
        }
    }

    private void setBatch(Configuration configuration, PlaywrightVisualSessionRequest.BatchMetadata batchMetadata) {
        BatchInfo batchInfo = new BatchInfo(batchMetadata.name());
        batchInfo.setId(batchMetadata.id());
        for (Map.Entry<String, String> entry : batchMetadata.properties().entrySet()) {
            batchInfo.addProperty(entry.getKey(), entry.getValue());
        }
        configuration.setBatch(batchInfo);
    }

    private EyesRunner createRunner(PlaywrightVisualSessionRequest request) {
        if (request.useUfg()) {
            return new VisualGridRunner(new RunnerOptions().testConcurrency(request.testConcurrency()));
        }
        return new ClassicRunner();
    }

    private void addUfgTargets(Configuration configuration, List<PlaywrightVisualSessionRequest.UfgTarget> ufgTargets) {
        for (PlaywrightVisualSessionRequest.UfgTarget ufgTarget : ufgTargets) {
            if (null != ufgTarget.browserType()) {
                configuration.addBrowser(ufgTarget.width(), ufgTarget.height(),
                        com.applitools.eyes.selenium.BrowserType.valueOf(ufgTarget.browserType()));
                continue;
            }
            configuration.addDeviceEmulation(DeviceName.fromName(ufgTarget.deviceName()),
                    null == ufgTarget.screenOrientation()
                            ? ScreenOrientation.PORTRAIT
                            : ScreenOrientation.valueOf(ufgTarget.screenOrientation()));
        }
    }

    private PlaywrightVisualResults.Entry toEntry(TestResultContainer container) {
        return new PlaywrightVisualResults.Entry(
                null == container.getBrowserInfo() ? null : container.getBrowserInfo().toString(),
                container.getTestResults());
    }

    private void setIfPresent(String value, java.util.function.Consumer<String> consumer) {
        if (null != value && !value.isBlank()) {
            consumer.accept(value);
        }
    }
}
