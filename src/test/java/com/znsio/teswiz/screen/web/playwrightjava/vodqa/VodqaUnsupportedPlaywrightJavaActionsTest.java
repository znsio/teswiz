package com.znsio.teswiz.screen.web.playwrightjava.vodqa;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class VodqaUnsupportedPlaywrightJavaActionsTest {
    @Test
    void shouldFailFastForVodqaScreenActions() {
        VodqaScreenPlaywrightJava screen =
                new VodqaScreenPlaywrightJava(mock(Driver.class), mock(Visual.class));

        assertThatThrownBy(screen::openDragAndDropScreen)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Vodqa")
                .hasMessageContaining("playwright-java");
        assertThatThrownBy(() -> screen.isPinchAndZoomInSuccessful(new Dimension(1, 1)))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Vodqa")
                .hasMessageContaining("playwright-java");
    }

    @Test
    void shouldFailFastForDragAndDropScreenActions() {
        DragAndDropScreenPlaywrightJava screen =
                new DragAndDropScreenPlaywrightJava(mock(Driver.class), mock(Visual.class));

        assertThatThrownBy(screen::dragAndDropCircleObject)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("drag and drop")
                .hasMessageContaining("playwright-java");
    }

    @Test
    void shouldFailFastForNativeViewScreenActions() {
        NativeViewScreenPlaywrightJava screen =
                new NativeViewScreenPlaywrightJava(mock(Driver.class), mock(Visual.class));

        assertThatThrownBy(screen::isUserOnNativeViewScreen)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("native view")
                .hasMessageContaining("playwright-java");
    }

    @Test
    void shouldFailFastForWebViewScreenActions() {
        WebViewScreenPlaywrightJava screen =
                new WebViewScreenPlaywrightJava(mock(Driver.class), mock(Visual.class));

        assertThatThrownBy(screen::navigateToSamplesList)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Vodqa")
                .hasMessageContaining("playwright-java");
    }
}
