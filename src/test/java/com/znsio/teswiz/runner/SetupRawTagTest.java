package com.znsio.teswiz.runner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SetupRawTagTest {
    private static final String cliConfigFilePath = "./configs/cli_local_config.properties";

    @AfterEach
    void clearTagOverride() {
        System.clearProperty(Setup.TAG);
    }

    @Test
    void shouldReturnNotSetWhenNoTagIsProvided() {
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();

        assertThat(Setup.getRawTagBeforeCucumberInference()).isEqualTo(Runner.NOT_SET);
    }

    @Test
    void shouldReturnExactlyTheUserProvidedTagIgnoringCucumberInferenceSuffixes() {
        System.setProperty(Setup.TAG, "@calculator");
        Setup.load(cliConfigFilePath);
        List<String> cukeArgs = Setup.getExecutionArguments();

        assertThat(cukeArgs).anySatisfy(arg -> assertThat(arg).contains("and not @wip"));
        assertThat(Setup.getRawTagBeforeCucumberInference()).isEqualTo("@calculator");
    }

    @Test
    void shouldReturnExactlyTheUserProvidedMultiTagExpression() {
        System.setProperty(Setup.TAG, "@cli and not @wip");
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();

        assertThat(Setup.getRawTagBeforeCucumberInference()).isEqualTo("@cli and not @wip");
    }
}
