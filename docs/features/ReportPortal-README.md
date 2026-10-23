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
* Selenium browser logs when the active web engine is `selenium`
* Playwright artifacts such as trace, HAR, and console logs when the active web engine is `playwright-java` or `playwright-ts`
* Appium device logs when they are available for the active mobile session

The attached session summary and session artifact messages include the following session details where available:

* persona
* platform
* engine
* provider
* session id

This makes it easier to inspect mixed Selenium, Playwright, and Appium scenarios in ReportPortal without guessing which backend or persona produced a given artifact.

For suite-level HTML reporting, teswiz also aggregates provider-native session and artifact URLs from
`scenario-session-metadata.json`, including report links plus normalized console, network, video, Playwright-log,
command-log, and screenshot URLs when the active provider exposes them.

At launch level, teswiz also publishes ReportPortal attributes such as:

* `Platform`
* `Provider`
* `WebEngine` for web and electron runs

This allows launch filtering and quick run identification before opening individual scenario artifacts.
