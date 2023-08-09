# Installing reportportal.io on your local machine

To install reportportal on local machine, refer to https://reportportal.io/installation. (Docker setup is the easiest way to proceed).

# Logging to ReportPortal

To make it easy to log to ReportPortal, the following methods are available:

```
        ReportPortalLogger.logDebugMessage("debugMessage");
        ReportPortalLogger.logInfoMessage("infoMessage");
        ReportPortalLogger.logWarningMessage("warningMessage");
        ReportPortalLogger.attachFileInReportPortal("message", new File("fileName"));