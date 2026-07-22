package com.znsio.teswiz.screen.theapp;


import java.util.Map;

public abstract class FileUploadScreen {

    public static FileUploadScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(FileUploadScreen.class);
    }

    public abstract FileUploadScreen navigateToFileUplaodPage();

    public abstract FileUploadScreen uploadFile(Map file);

    public abstract String getFileUploadText();
}
