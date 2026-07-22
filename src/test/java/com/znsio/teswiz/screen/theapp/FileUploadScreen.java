package com.znsio.teswiz.screen.theapp;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.web.theapp.FileUploadScreenWeb;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public abstract class FileUploadScreen {
    private static final String SCREEN_NAME = FileUploadScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static FileUploadScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(FileUploadScreen.class);
    }

    public abstract FileUploadScreen navigateToFileUplaodPage();

    public abstract FileUploadScreen uploadFile(Map file);

    public abstract String getFileUploadText();
}
