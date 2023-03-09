package com.znsio.teswiz.tools;

import com.epam.reportportal.service.ReportPortal;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Date;

import static com.znsio.teswiz.runner.Runner.*;

public class ReportPortalLogger {
    private static final Logger LOGGER = Logger.getLogger(ReportPortalLogger.class.getName());

    private ReportPortalLogger() {
        LOGGER.debug("ReportPortalLogger - private constructor");
    }

    public static void attachFileInReportPortal(String message, File destinationFile) {
        boolean isEmitLogSuccessful = ReportPortal.emitLog(message, DEBUG, new Date(),
                                                           destinationFile);
        if(isEmitLogSuccessful) {
            LOGGER.debug(
                    String.format("'%s' - Upload of file: '%s'::'%s' to ReportPortal succeeded",
                                  getCallingClassAndMethodName(), message, destinationFile));
            System.out.printf("'%s' - Upload of file: '%s'::'%s' to ReportPortal succeeded%n",
                              getCallingClassAndMethodName(), message, destinationFile);
        } else {
            LOGGER.error(String.format("'%s' - Upload of file: '%s'::'%s' to ReportPortal failed",
                                       getCallingClassAndMethodName(), message, destinationFile));
            System.out.printf("'%s' - Upload of file: '%s'::'%s' to ReportPortal failed%n",
                              getCallingClassAndMethodName(), message, destinationFile);
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
        boolean isEmitLogSuccessful = ReportPortal.emitLog(message, level, new Date());
        if(isEmitLogSuccessful) {
            System.out.printf("'%s' - Logging message: '%s' to ReportPortal succeeded%n",
                              getCallingClassAndMethodName(), message);
            LOGGER.debug(String.format("'%s' - Logging message: '%s' to ReportPortal succeeded",
                                       getCallingClassAndMethodName(), message));

        } else {
            System.out.printf("'%s' - Logging message: '%s' to ReportPortal failed%n",
                              getCallingClassAndMethodName(), message);
            LOGGER.error(String.format("'%s' - Logging message: '%s' to ReportPortal failed",
                                       getCallingClassAndMethodName(), message));
        }
    }
}
