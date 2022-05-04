package com.znsio.e2e.screen.android;

import com.znsio.e2e.screen.NotepadScreen;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

public class NotepadScreenAndroid extends NotepadScreen {
    private static final Logger LOGGER = Logger.getLogger(NotepadScreenAndroid.class.getName());
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = NotepadScreenAndroid.class.getSimpleName();
    private final By byEditorName = By.name("Text Editor");

    public NotepadScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public NotepadScreen takeScreenshot() {
        visually.takeScreenshot(SCREEN_NAME, "Notepad launched");
        return this;
    }

    @Override
    public NotepadScreen typeMessage(String message) {
        return this;
    }
}
