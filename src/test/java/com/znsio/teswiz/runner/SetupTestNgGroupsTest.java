package com.znsio.teswiz.runner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SetupTestNgGroupsTest {
    private static final String cliConfigFilePath = "./configs/cli_local_config.properties";

    @AfterEach
    void clearTagOverride() {
        System.clearProperty(Setup.TAG);
    }

    @Test
    void shouldReturnNoGroupsWhenNoTagIsProvided() {
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();

        assertThat(Setup.getTestNgGroups()).isEmpty();
    }

    @Test
    void shouldReturnSingleGroupWhenSingleTagIsProvided() {
        System.setProperty(Setup.TAG, "@calculator");
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();

        assertThat(Setup.getTestNgGroups()).containsExactly("calculator");
    }

    @Test
    void shouldReturnMultipleGroupsWhenMultipleTagsAreProvided() {
        System.setProperty(Setup.TAG, "@cli @calculator");
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();

        assertThat(Setup.getTestNgGroups()).containsExactly("cli", "calculator");
    }

    @Test
    void shouldReturnOnlyTheRawUserProvidedTagIgnoringCucumberInferenceSuffixes() {
        System.setProperty(Setup.TAG, "@calculator");
        Setup.load(cliConfigFilePath);
        List<String> cukeArgs = Setup.getExecutionArguments();

        assertThat(cukeArgs).anySatisfy(arg -> assertThat(arg).contains("and not @wip"));
        assertThat(Setup.getTestNgGroups()).containsExactly("calculator");
    }
}
