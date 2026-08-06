package com.znsio.teswiz.screen.web.playwrightjava.notepad;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.notepad.NotepadScreen;

public class NotepadScreenPlaywrightJava extends NotepadScreen {
    private static final String ENGINE_NAME = "playwright-java";

    public NotepadScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public NotepadScreen typeMessage(String message) {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "Notepad is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
