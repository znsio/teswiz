package com.znsio.teswiz.screen.generateddemo;

import java.util.List;

public abstract class GeneratedPlaywrightTsTestScreen {

    public abstract GeneratedPlaywrightTsTestScreen open(String url);

    public abstract GeneratedPlaywrightTsTestScreen enterValue(String value);

    public abstract String readValue();

    public abstract List<String> readValues();
}
