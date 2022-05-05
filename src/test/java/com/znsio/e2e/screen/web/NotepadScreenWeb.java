package com.znsio.e2e.screen.web;

import com.znsio.e2e.screen.NotepadScreen;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

public class NotepadScreenWeb extends NotepadScreen {
    private static final Logger LOGGER = Logger.getLogger(NotepadScreenWeb.class.getName());
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = NotepadScreenWeb.class.getSimpleName();
    private final By byEditorName = By.name("Text Editor");

    public NotepadScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public NotepadScreen takeScreenshot() {
        visually.checkWindow(SCREEN_NAME, "takeScreenshot");
        return this;
    }

    @Override
    public NotepadScreen typeMessage(String message) {
        return this;
    }
}
