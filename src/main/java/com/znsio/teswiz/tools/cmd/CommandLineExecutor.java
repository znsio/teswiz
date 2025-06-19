package com.znsio.teswiz.tools.cmd;

import com.znsio.teswiz.exceptions.CommandLineExecutorException;
import com.znsio.teswiz.runner.Runner;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class CommandLineExecutor {
    private static final Logger LOGGER = LogManager.getLogger(CommandLineExecutor.class.getName());
    private static final int DEFAULT_COMMAND_TIMEOUT = 60;

    private CommandLineExecutor() {}

    public static CommandLineResponse execCommand(final String[] command) {
        return execCommand(command, DEFAULT_COMMAND_TIMEOUT);
    }

    public static CommandLineResponse execCommand(final String[] command, int timeoutInSeconds) {
        String jointCommand = String.join(" ", command);
        String message = "\tExecuting Command: " + jointCommand;
        try {
            CommandLineResponse response = new CommandLineResponse();
            ProcessBuilder builder = new ProcessBuilder(command);
            if(Runner.IS_WINDOWS) {
                builder.command("cmd.exe", "/c", jointCommand);
            } else {
                builder.command("sh", "-c", jointCommand);
            }
            Process process = builder.start();
            process.waitFor(timeoutInSeconds, TimeUnit.SECONDS);
            response.setStdOut(
                    IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8).trim());
            response.setErrOut(
                    IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8).trim());
            String responseMessage = String.format("\tExit code: %d %n\tResponse:%n%s\t", process.exitValue(), response);
            LOGGER.info(responseMessage);
            response.setExitCode(process.exitValue());
            return response;
        } catch(IOException | InterruptedException e) {
            throw new CommandLineExecutorException("Error " + message, e);
        }
    }
}
