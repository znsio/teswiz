package com.znsio.teswiz.screen.web.playwrightjava.theapp;

import java.util.Map;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.theapp.FileUploadScreen;

public class FileUploadScreenPlaywrightJava extends FileUploadScreen {
    private static final By FILE_UPLOAD_LINK = By.xpath("//a[@href=\"/upload\"]");
    private static final By CHOOSE_FILE_INPUT = By.xpath("//input[@name=\"file\"]");
    private static final By UPLOAD_BUTTON = By.id("file-submit");
    private static final By FILE_UPLOAD_MESSAGE = By.xpath("//h3[contains(text(),'File Uploaded!')]");

    private final Driver driver;

    public FileUploadScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
    }

    @Override
    public FileUploadScreen navigateToFileUplaodPage() {
        driver.waitTillElementIsVisible(FILE_UPLOAD_LINK).click();
        return this;
    }

    @Override
    public FileUploadScreen uploadFile(Map file) {
        String filePath = System.getProperty("user.dir") + file.get("IMAGE_FILE_LOCATION");
        driver.uploadFileInBrowser(filePath, CHOOSE_FILE_INPUT);
        driver.findElement(UPLOAD_BUTTON).submit();
        return this;
    }

    @Override
    public String getFileUploadText() {
        return driver.waitTillElementIsPresent(FILE_UPLOAD_MESSAGE).getText();
    }
}
