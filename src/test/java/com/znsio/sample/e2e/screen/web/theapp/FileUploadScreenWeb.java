package com.znsio.sample.e2e.screen.web.theapp;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.theapp.FileUploadScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

import java.util.Map;

public class FileUploadScreenWeb extends FileUploadScreen {
    private static final Logger LOGGER = Logger.getLogger(FileUploadScreenWeb.class.getName());
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = FileUploadScreenWeb.class.getSimpleName();
    private final By byFileUploadXpath= By.xpath("//a[@href=\"/upload\"]");
    private final By byChosseFileXpath= By.xpath("//input[@name=\"file\"]");
    private final By byUplaodButtonId= By.id("file-submit");
    private final By byFileUploadMessageXpath= By.xpath("//h3[contains(text(),'File Uploaded!')]");

    public FileUploadScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public FileUploadScreen navigateToFileUplaodPage() {
        driver.waitTillElementIsVisible(byFileUploadXpath).click();
        return this;
    }

    @Override
    public FileUploadScreen uploadFile(Map file) {
        String filePath = System.getProperty("user.dir") + file.get("IMAGE_FILE_LOCATION");
        driver.uploadFileInBrowser(filePath,byChosseFileXpath);
        driver.findElement(byUplaodButtonId).submit();
        return this;
    }

    @Override
    public String getFileUploadText() {
       return driver.waitTillElementIsPresent(byFileUploadMessageXpath).getText();
    }
}
