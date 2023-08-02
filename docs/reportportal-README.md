# Logging to ReportPortal

To make it easy to log to ReportPortal, the following methods are available:

```
        ReportPortalLogger.logDebugMessage("debugMessage");
        ReportPortalLogger.logInfoMessage("infoMessage");
        ReportPortalLogger.logWarningMessage("warningMessage");
        ReportPortalLogger.attachFileInReportPortal("message", new File("fileName"));