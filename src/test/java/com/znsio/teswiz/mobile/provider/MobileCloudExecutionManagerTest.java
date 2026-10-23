package com.znsio.teswiz.mobile.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.znsio.teswiz.exceptions.InvalidTestDataException;

class MobileCloudExecutionManagerTest {
    @Test
    void shouldRouteSetupToMatchingProviderAction() {
        List<String> invokedActions = new ArrayList<>();
        MobileCloudExecutionManager manager = new MobileCloudExecutionManager(
                apiUrl -> invokedActions.add("browserstack:" + apiUrl),
                apiUrl -> invokedActions.add("lambdatest:" + apiUrl),
                apiUrl -> invokedActions.add("headspin:" + apiUrl),
                apiUrl -> invokedActions.add("pcloudy:" + apiUrl),
                () -> invokedActions.add("browserstack-cleanup"));

        manager.setupCloudExecution("lambdatest", "https://manual-api.lambdatest.com");

        assertThat(invokedActions).containsExactly("lambdatest:https://manual-api.lambdatest.com");
    }

    @Test
    void shouldCleanupBrowserStackUsingMatchingProviderAction() {
        List<String> invokedActions = new ArrayList<>();
        MobileCloudExecutionManager manager = new MobileCloudExecutionManager(
                apiUrl -> invokedActions.add("browserstack:" + apiUrl),
                apiUrl -> invokedActions.add("lambdatest:" + apiUrl),
                apiUrl -> invokedActions.add("headspin:" + apiUrl),
                apiUrl -> invokedActions.add("pcloudy:" + apiUrl),
                () -> invokedActions.add("browserstack-cleanup"));

        manager.cleanupCloudExecution("browserstack");

        assertThat(invokedActions).containsExactly("browserstack-cleanup");
    }

    @Test
    void shouldIgnoreCleanupForProvidersWithoutCleanupRequirements() {
        List<String> invokedActions = new ArrayList<>();
        MobileCloudExecutionManager manager = new MobileCloudExecutionManager(
                apiUrl -> invokedActions.add("browserstack:" + apiUrl),
                apiUrl -> invokedActions.add("lambdatest:" + apiUrl),
                apiUrl -> invokedActions.add("headspin:" + apiUrl),
                apiUrl -> invokedActions.add("pcloudy:" + apiUrl),
                () -> invokedActions.add("browserstack-cleanup"));

        manager.cleanupCloudExecution("lambdatest");

        assertThat(invokedActions).isEmpty();
    }

    @Test
    void shouldFailForUnsupportedCloudDuringSetup() {
        MobileCloudExecutionManager manager = new MobileCloudExecutionManager(
                apiUrl -> {
                },
                apiUrl -> {
                },
                apiUrl -> {
                },
                apiUrl -> {
                },
                () -> {
                });

        assertThatThrownBy(() -> manager.setupCloudExecution("unsupported-cloud", "https://example.com"))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessage("Provided cloudName: 'unsupported-cloud' is not supported");
    }

    @Test
    void shouldFailForUnsupportedCloudDuringCleanup() {
        MobileCloudExecutionManager manager = new MobileCloudExecutionManager(
                apiUrl -> {
                },
                apiUrl -> {
                },
                apiUrl -> {
                },
                apiUrl -> {
                },
                () -> {
                });

        assertThatThrownBy(() -> manager.cleanupCloudExecution("unsupported-cloud"))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessage("Provided cloudName: 'unsupported-cloud' is not supported");
    }
}
