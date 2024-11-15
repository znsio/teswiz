package com.znsio.teswiz.tools.cmd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.znsio.teswiz.runner.Runner.NOT_SET;
import static org.assertj.core.api.Assertions.assertThat;

class AsyncCommandLineExecutorTest {
    private static final Logger LOGGER = LogManager.getLogger(AsyncCommandLineExecutorTest.class.getName());
    private static int commmandNumber = 1;
    private static final String LOG_DIR = "./target/testLogs";

    @BeforeAll
    public static void setupBefore() {
        LOGGER.info("Create LOG_DIR: " + LOG_DIR);
        System.setProperty("LOG_DIR", LOG_DIR);
        new File(LOG_DIR).mkdirs();
    }


    @Test
    void asyncCLICalculatorTest() {
        try {
            AsyncCommandLineExecutor executor = new AsyncCommandLineExecutor();
            calculatorTest(executor);
            executor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String sendCommand(AsyncCommandLineExecutor executor, String command, int timeoutInSeconds) {
        LOGGER.info("Command # " + commmandNumber++);
        AsyncCommandLineExecutor.CommandResult result = executor.sendCommand(command, timeoutInSeconds);
        LOGGER.info("%n--> Stdout: %n%s".formatted(result.getOutput()));
        return result.getOutput();
    }

    private static void calculatorTest(AsyncCommandLineExecutor executor) {
        String response = NOT_SET;

        String command = "pwd";
        int timeoutInSeconds = 1;
        String expectedResponse = "/Users/anand.bagmar/projects/znsio/teswiz";

        response = sendCommand(executor, command, timeoutInSeconds);
        assertThat(response).as("Output of command: '" + command + "' is incorrect").isEqualTo(expectedResponse);

        command = "./src/test/java/com/znsio/teswiz/tools/cmd/calculator.sh";
        timeoutInSeconds = 2;
        expectedResponse = """
                ----------------------------------------------------
                Welcome to the Simple Command Line Calculator
                Please choose an option:
                1. Add
                2. Subtract
                3. Exit
                4. Log Errors
                ----------------------------------------------------""";
        response = sendCommand(executor, command, timeoutInSeconds);
        assertThat(response).as("Output of command: '" + command + "' is incorrect").isEqualTo(expectedResponse);

        command = "1";
        timeoutInSeconds = 2;
        expectedResponse = """
                Addition:
                
                Enter first number:""";
        response = sendCommand(executor, command, timeoutInSeconds);
        assertThat(response).as("Output of command: '" + command + "' is incorrect").isEqualTo(expectedResponse);

        command = "15";
        timeoutInSeconds = 2;
        expectedResponse = "Enter second number:";
        response = sendCommand(executor, command, timeoutInSeconds);
        assertThat(response).as("Output of command: '" + command + "' is incorrect").isEqualTo(expectedResponse);

        command = "24";
        timeoutInSeconds = 2;
        expectedResponse = """
                [32mResult: 39[0m
                ----------------------------------------------------
                Welcome to the Simple Command Line Calculator
                Please choose an option:
                1. Add
                2. Subtract
                3. Exit
                4. Log Errors
                ----------------------------------------------------""";
        response = sendCommand(executor, command, timeoutInSeconds);
        assertThat(response).as("Output of command: '" + command + "' is incorrect").isEqualTo(expectedResponse);

        command = "4";
        expectedResponse = """
                Logging error to stderr in red.
                ----------------------------------------------------
                Welcome to the Simple Command Line Calculator
                Please choose an option:
                1. Add
                2. Subtract
                3. Exit
                4. Log Errors
                ----------------------------------------------------""";
        response = sendCommand(executor, command, timeoutInSeconds);
        assertThat(response).as("Output of command: '" + command + "' is incorrect").isEqualTo(expectedResponse);

        command = "5";
        expectedResponse = """
                Invalid option. Please try again.
                ----------------------------------------------------
                Welcome to the Simple Command Line Calculator
                Please choose an option:
                1. Add
                2. Subtract
                3. Exit
                4. Log Errors
                ----------------------------------------------------""";
        response = sendCommand(executor, command, timeoutInSeconds);
        assertThat(response).as("Output of command: '" + command + "' is incorrect").isEqualTo(expectedResponse);

        command = "3";
        expectedResponse = """
                Exiting the calculator. Goodbye!""";
        response = sendCommand(executor, command, timeoutInSeconds);
        assertThat(response).as("Output of command: '" + command + "' is incorrect").isEqualTo(expectedResponse);
    }
}
