package com.znsio.teswiz.steps;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;

class FigmaStepsTest {

    @Test
    void addFigmaDesignDetailsToContextShouldStoreAllValues() {
        TestExecutionContext context = new TestExecutionContext("figma-step");
        FigmaSteps figmaSteps = new FigmaSteps();

        figmaSteps.addFigmaDesignDetailsToContext("Applitools", "Important pages", "vodqa_screens");

        assertThat(context.getTestStateAsString(TEST_CONTEXT.APPLITOOLS_FIGMA_APP_NAME))
                .isEqualTo("Applitools");
        assertThat(context.getTestStateAsString(TEST_CONTEXT.APPLITOOLS_FIGMA_TEST_NAME))
                .isEqualTo("Important pages");
        assertThat(context.getTestStateAsString(TEST_CONTEXT.APPLITOOLS_FIGMA_BASELINE_ENV_NAME))
                .isEqualTo("vodqa_screens");
    }
}
