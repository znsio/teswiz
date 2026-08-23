package com.znsio.teswiz.steps;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.testng.TestNgTestExecutionContextFactory;
import io.cucumber.java.Scenario;
import io.cucumber.java.Status;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class HooksTestNgOverloadTest {
    private static final String cliConfigFilePath = "./configs/cli_local_config.properties";

    @Test
    void stringOverloadShouldInitialiseTheSameStateAsScenarioOverload() {
        TestExecutionContext scenarioBasedContext = givenALoadedTestExecutionContext("hooks-scenario-overload-test");
        Scenario scenario = Mockito.mock(Scenario.class);
        when(scenario.getName()).thenReturn("scenario-based-test");
        when(scenario.getStatus()).thenReturn(Status.PASSED);

        new Hooks().beforeScenario(scenario);
        assertHooksInitialisedState(scenarioBasedContext);
        new Hooks().afterScenario(scenario);
        assertThat(scenarioBasedContext.getTestState(TEST_CONTEXT.HOOKS_INITIALIZED)).isNull();

        TestExecutionContext stringBasedContext = givenALoadedTestExecutionContext("hooks-string-overload-test");
        new Hooks().beforeScenario("string-based-test");
        assertHooksInitialisedState(stringBasedContext);
        new Hooks().afterScenario("string-based-test", false);
        assertThat(stringBasedContext.getTestState(TEST_CONTEXT.HOOKS_INITIALIZED)).isNull();
    }

    private TestExecutionContext givenALoadedTestExecutionContext(String testName) {
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();
        long threadId = Thread.currentThread().getId();
        TestExecutionContext context = TestNgTestExecutionContextFactory.create(testName, 1);
        assertThat(Runner.getTestExecutionContext(threadId)).isSameAs(context);
        return context;
    }

    private void assertHooksInitialisedState(TestExecutionContext context) {
        assertThat(context.getTestState(TEST_CONTEXT.HOOKS_INITIALIZED)).isEqualTo(true);
        assertThat(context.getTestState(TEST_CONTEXT.SCREENSHOT_MANAGER)).isNotNull();
        assertThat(context.getTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS)).isNotNull();
        assertThat(context.getTestState(TEST_CONTEXT.SOFT_ASSERTIONS)).isNotNull();
        assertThat(context.getTestState(TEST_CONTEXT.ASYNC_COMMAND_LINE_EXECUTOR)).isNotNull();
    }
}
