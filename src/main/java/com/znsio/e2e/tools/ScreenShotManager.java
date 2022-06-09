package com.znsio.e2e.tools;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.epam.reportportal.service.ReportPortal;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.runner.Runner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class ScreenShotManager {

    private static final Logger LOGGER = Logger.getLogger(ScreenShotManager.class.getName());
    private final TestExecutionContext context;
    private final String directoryPath;
    private int counter;

    public ScreenShotManager() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread()
                                                               .getId());
        directoryPath = context.getTestStateAsString(TEST_CONTEXT.SCREENSHOT_DIRECTORY);
        counter = 0;
        File file = new File(directoryPath);
        file.getParentFile()
            .mkdirs();
    }

    public void takeScreenShot(WebDriver innerDriver, String fileName) {
        if(null != innerDriver) {
            fileName = normaliseScenarioName(getPrefix() + "-" + fileName);
            File destinationFile = createScreenshotFile(directoryPath, fileName);
            LOGGER.info("The screenshot will be placed here : " + destinationFile.getAbsolutePath());
            try {
                File screenshot = ((TakesScreenshot) innerDriver).getScreenshotAs(OutputType.FILE);
                LOGGER.info("Original screenshot : " + screenshot.getAbsolutePath());
                FileUtils.copyFile(screenshot, destinationFile);
                LOGGER.info("The screenshot is available here : " + destinationFile.getAbsolutePath());
                ReportPortal.emitLog(fileName, "DEBUG", new Date(), destinationFile);
            } catch(IOException | RuntimeException e) {
                LOGGER.info("ERROR: Unable to save or upload screenshot: '" + destinationFile.getAbsolutePath() + "' or upload screenshot to ReportPortal\n");
                LOGGER.info(ExceptionUtils.getStackTrace(e));
            }
        } else {
            LOGGER.info("Driver is not instantiated for this test");
        }
    }

    private String normaliseScenarioName(String scenarioName) {
        return scenarioName.replaceAll("[`~ !@#$%^&*()\\-=+\\[\\]{}\\\\|;:'\",<.>/?]", "_")
                           .replaceAll("__", "_")
                           .replaceAll("__", "_");
    }

    private int getPrefix() {
        return ++counter;
    }

    private File createScreenshotFile(String dirName, String fileName) {
        fileName = fileName.endsWith(".png") ? fileName : fileName + ".png";
        return new File(Runner.USER_DIRECTORY + dirName + File.separator + fileName);
    }
}
