# Installing reportportal.io on your local machine

To install reportportal on local machine, refer to https://reportportal.io/installation. (Docker setup is the easiest way to proceed).

# Logging to ReportPortal

To make it easy to log to ReportPortal, the following methods are available:

```
        ReportPortalLogger.logDebugMessage("debugMessage");
        ReportPortalLogger.logInfoMessage("infoMessage");
        ReportPortalLogger.logWarningMessage("warningMessage");
        ReportPortalLogger.attachFileInReportPortal("message", new File("fileName"));
```

teswiz also attaches scenario-level reporting artifacts automatically at the end of execution.

For dual-engine and multi-user runs, this includes:

* `scenario-session-metadata.json`
* `scenario-session-summary.txt`
* Applitools visual logs such as `applitools-web.log` when visual execution generates them
* Playwright artifacts such as trace, HAR, and console logs when the active web engine is `playwright-java` or `playwright-ts`

The attached session summary and Playwright artifact messages include the following session details where available:

* persona
* platform
* engine
* provider
* session id

This makes it easier to inspect mixed Selenium, Playwright, and Appium scenarios in ReportPortal without guessing which backend or persona produced a given artifact.

At launch level, teswiz also publishes ReportPortal attributes such as:

* `Platform`
* `Provider`
* `WebEngine` for web and electron runs

This allows launch filtering and quick run identification before opening individual scenario artifacts.
