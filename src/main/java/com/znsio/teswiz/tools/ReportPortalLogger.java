package com.znsio.teswiz.tools;

import com.epam.reportportal.service.ReportPortal;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Date;

import static com.znsio.teswiz.runner.Runner.*;

public class ReportPortalLogger {
    private static final Logger LOGGER = Logger.getLogger(ReportPortalLogger.class.getName());

    public static void attachFileInReportPortal(String message, File destinationFile) {
        if(!ReportPortal.emitLog(message, DEBUG, new Date(), destinationFile)) {
            LOGGER.error(String.format("'%s' - Upload of file: '%s'::'%s' to reportportal failed",
                                       getCallingClassAndMethodName(), message, destinationFile));
        }
    }

    public static void logDebugMessage(String message) {
        logMessage(message, DEBUG);
    }

    public static void logWarningMessage(String message) {
        logMessage(message, WARN);
    }

    public static void logInfoMessage(String message) {
        logMessage(message, INFO);
    }

    private static String getCallingClassAndMethodName() {
        try {
            final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
            return ste[3].getClassName() + ":" + ste[3].getMethodName();
        } catch(ArrayIndexOutOfBoundsException e) {
            LOGGER.error("Unable to get calling method class/method name");
            return "teswiz";
        }
    }

    private static void logMessage(String message, String level) {
        if(!ReportPortal.emitLog(message, level, new Date())) {
            LOGGER.error(String.format("'%s' - Logging message: '%s' to reportportal failed",
                                       getCallingClassAndMethodName(), message));
        }
    }
}
