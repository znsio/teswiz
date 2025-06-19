package com.znsio.teswiz.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YamlFileTest {
    private static final Logger LOGGER = LogManager.getLogger(YamlFileTest.class.getName());

    @BeforeAll
    public static void setupBefore() {
        LOGGER.info("Running YamlFileTest");
    }

    @Test
    void compareIdenticalFiles() {
        assertThat(YamlFile.compareFiles(".github/workflows/HardGate_CI.yml", ".github/workflows/HardGate_CI.yml")).as("Files are not identical").isTrue();
    }

    @Test
    void compareDifferentFiles() {
        assertThat(YamlFile.compareFiles(".github/workflows/HardGate_CI.yml", ".github/workflows/InteractiveCLI_CI.yml")).as("Files are not identical").isFalse();
    }
}
