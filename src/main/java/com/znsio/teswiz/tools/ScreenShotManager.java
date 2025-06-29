package com.znsio.teswiz.tools;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;

public class ScreenShotManager {

    private static final Logger LOGGER = LogManager.getLogger(ScreenShotManager.class.getName());
    private final TestExecutionContext context;
    private final String directoryPath;
    private int counter;

    public ScreenShotManager() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        directoryPath = context.getTestStateAsString(TEST_CONTEXT.SCREENSHOT_DIRECTORY);
        counter = 0;
        com.znsio.teswiz.tools.FileUtils.createDirectory(directoryPath);
    }

    public void takeScreenShot(WebDriver driver, String fileName) {
        if (null != driver) {
            fileName = normaliseScenarioName(getPrefix() + "-" + fileName);
            File destinationFile = createScreenshotFile(directoryPath, fileName);
            LOGGER.debug("The screenshot will be placed here : {}", destinationFile.getAbsolutePath());
            try {
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                LOGGER.debug("Original screenshot : {}", screenshot.getAbsolutePath());
                FileUtils.copyFile(screenshot, destinationFile);
                LOGGER.info("The screenshot is available here : {}", destinationFile.getAbsolutePath());
                ReportPortalLogger.attachFileInReportPortal(fileName, destinationFile);
            } catch (RuntimeException e) {
                LOGGER.warn("ERROR: Unable to save or upload screenshot: '{}' or upload screenshot to ReportPortal\n", destinationFile.getAbsolutePath());
                LOGGER.debug(ExceptionUtils.getStackTrace(e));
            }
        } else {
            LOGGER.warn("Driver is not instantiated for this test");
        }
    }

    private String normaliseScenarioName(String scenarioName) {
        return scenarioName.replaceAll("[`~ !@#$%^&*()\\-=+\\[\\]{}\\\\|;:'\",<.>/?]", "_")
                .replaceAll("__", "_").replaceAll("__", "_");
    }

    private int getPrefix() {
        return ++counter;
    }

    private File createScreenshotFile(String dirName, String fileName) {
        fileName = fileName.endsWith(".png") ? fileName : fileName + ".png";
        return new File(dirName + File.separator + fileName);
    }
}
