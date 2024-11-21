package com.znsio.teswiz.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class YamlFileTest {
    private static final Logger LOGGER = LogManager.getLogger(YamlFileTest.class.getName());
    private static final String LOG_DIR = "./target/testLogs";

    @BeforeAll
    public static void setupBefore() {
        LOGGER.info("Create LOG_DIR: " + LOG_DIR);
        System.setProperty("LOG_DIR", LOG_DIR);
        new File(LOG_DIR).mkdirs();
    }

    @Test
    void compareIdenticalFiles() {
        assertThat(YamlFile.compareFiles(".github/workflows/HardGate_PassingBuild.yml", ".github/workflows/HardGate_PassingBuild.yml")).as("Files are not identical").isTrue();
    }

    @Test
    void compareDifferentFiles() {
        assertThat(YamlFile.compareFiles(".github/workflows/HardGate_PassingBuild.yml", ".github/workflows/HardGate_FailingBuild.yml")).as("Files are not identical").isFalse();
    }
}
