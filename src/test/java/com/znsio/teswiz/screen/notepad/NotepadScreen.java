package com.znsio.teswiz.screen.notepad;


public abstract class NotepadScreen {

    public static NotepadScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(NotepadScreen.class);
    }

    public abstract NotepadScreen typeMessage(String message);
}
