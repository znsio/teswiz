package com.znsio.teswiz.tools.cmd;

import com.znsio.teswiz.runner.Runner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncCommandLineExecutor {
    private final Process process;
    private final PrintWriter commandWriter;
    private final BufferedReader outputReader;
    private static final Logger LOGGER = LogManager.getLogger(AsyncCommandLineExecutor.class.getName());

    public static class CommandResult {
        private final String output;

        public CommandResult(String output) {
            this.output = output;
        }

        public String getOutput() {
            return output;
        }
    }

    public AsyncCommandLineExecutor() throws IOException {
        String os = Runner.OS_NAME.toLowerCase();

        ProcessBuilder processBuilder = os.contains("win")
                                        ? new ProcessBuilder("cmd.exe")
                                        : new ProcessBuilder("bash");

        processBuilder.redirectErrorStream(true);

        process = processBuilder.start();

        commandWriter = new PrintWriter(process.getOutputStream(), true);
        outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    public CommandResult sendCommand(String command, int timeoutInSeconds) {
        final String platformCommand = Runner.IS_WINDOWS
                                       ? "/c " + command
                                       : command;

        LOGGER.info("Executing command: " + platformCommand);

        StringBuilder output = new StringBuilder();

        // CompletableFuture for reading stdout asynchronously
        CompletableFuture<Void> stdoutFuture = CompletableFuture.runAsync(() -> {
            try {
                readStream(outputReader, output);
            } catch (IOException e) {
                LOGGER.debug("Error reading stdout: " + e.getMessage());
            }
        });

        // Write the command to the shell
        commandWriter.println(platformCommand);
        commandWriter.flush();

        try {
            // Wait for the command to complete or timeout
            boolean finished = process.waitFor(timeoutInSeconds, TimeUnit.SECONDS);

            if (!finished) {
                LOGGER.debug("Command timed out. Returning partial output if any.");
            }

            // Wait for the stdoutFuture to complete (even if the process timed out)
            stdoutFuture.get(timeoutInSeconds, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            LOGGER.debug("Command timed out. Returning partial output if any.");
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            LOGGER.debug(e.getMessage(), e);
        }

        return new CommandResult(output.toString().trim());
    }

    private void readStream(BufferedReader reader, StringBuilder output) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append(System.lineSeparator());
            LOGGER.info(line); // Print each line as it is read
        }
    }

    public void close() {
        try {
            if (process.isAlive()) {
                process.destroy();
            }
            commandWriter.close();
            outputReader.close();
        } catch (IOException e) {
            LOGGER.debug("Error closing resources: " + e.getMessage());
        }
    }
}
