package com.znsio.teswiz.tools.cmd;

import com.znsio.teswiz.exceptions.CommandLineExecutorException;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.tools.OsUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
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
        return execCommandInternal(jointCommand, timeoutInSeconds, true);
    }

    /**
     * Executes a command without forcing a shell wrapper.
     * Prefer this when you don't need shell features like pipes/redirection.
     */
    public static CommandLineResponse execCommand(final List<String> command, int timeoutInSeconds) {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("command cannot be null/empty");
        }
        // Build a nice display string, but execute without shell.
        String display = String.join(" ", command);
        return execCommandInternal(display, timeoutInSeconds, false, command);
    }

    /**
     * Best-effort check to see whether a command exists on PATH.
     * Works on Windows/Linux/macOS and in CI runners.
     */
    public static boolean isCommandAvailable(String tool) {
        if (tool == null || tool.trim().isEmpty()) return false;

        // 'where' on Windows, 'which' on Unix
        String trimmed = tool.trim();
        String[] cmd = OsUtils.isWindows()
                       ? new String[]{"where", trimmed}
                       : new String[]{"which", trimmed};

        try {
            CommandLineResponse r = execCommand(cmd, 5);
            return r.getExitCode() == 0 && r.getStdOut() != null && !r.getStdOut().trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Best-effort "tool works" check (covers cases where tool exists but isn't runnable).
     * Tries: <tool> --version then <tool> version
     */
    public static boolean toolWorks(String tool) {
        if (!isCommandAvailable(tool)) return false;

        try {
            CommandLineResponse r1 = execCommand(new String[]{tool, "--version"}, 5);
            if (r1.getExitCode() == 0) return true;

            CommandLineResponse r2 = execCommand(new String[]{tool, "version"}, 5);
            return r2.getExitCode() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static CommandLineResponse execCommandInternal(String jointCommand,
            int timeoutInSeconds,
            boolean useShell) {
        return execCommandInternal(jointCommand, timeoutInSeconds, useShell, null);
    }

    private static CommandLineResponse execCommandInternal(String displayCommand,
            int timeoutInSeconds,
            boolean useShell,
            List<String> directCommandParts) {
        String message = "\tExecuting Command: " + displayCommand;
        long start = System.nanoTime();

        CommandLineResponse response = new CommandLineResponse();
        response.setCommand(displayCommand);

        Process process = null;
        try {
            ProcessBuilder builder;

            if (useShell) {
                // Maintain legacy behavior: execute through shell to support pipes/redirects etc.

                if (OsUtils.isWindows()) {
                    builder = new ProcessBuilder("cmd.exe", "/c", displayCommand);
                } else {
                    builder = new ProcessBuilder("sh", "-c", displayCommand);
                }
            } else {
                // Execute directly (no shell). Safer for quoting/args.
                builder = new ProcessBuilder(directCommandParts);
            }

            process = builder.start();

            boolean finished = process.waitFor(timeoutInSeconds, TimeUnit.SECONDS);
            if (!finished) {
                // IMPORTANT: avoid IllegalThreadStateException from exitValue()
                response.setTimedOut(true);
                response.setExitCode(124); // common "timeout" convention
                // Kill process to free CI agents/VMs
                process.destroy();
                process.waitFor(2, TimeUnit.SECONDS);
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            } else {
                response.setExitCode(process.exitValue());
            }

            // Read outputs (best effort). Even on timeout, streams may contain partial output.
            response.setStdOut(safeRead(process, true));
            response.setErrOut(safeRead(process, false));

            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            response.setDurationMillis(durationMs);

            String responseMessage = String.format(
                    "\tExit code: %d (timedOut=%s, durationMs=%d)%n\tResponse:%n%s\t",
                    response.getExitCode(),
                    response.isTimedOut(),
                    response.getDurationMillis(),
                    response
            );
            LOGGER.info(responseMessage);

            return response;

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt(); // safe even if not interrupted
            throw new CommandLineExecutorException("Error " + message, e);
        } catch (RuntimeException e) {
            // Catch unexpected things like IllegalThreadStateException, etc.
            throw new CommandLineExecutorException("Error " + message + " :: " + e, e);
        } finally {
            if (process != null) {
                try { process.getInputStream().close(); } catch (Exception ignored) {}
                try { process.getErrorStream().close(); } catch (Exception ignored) {}
                try { process.getOutputStream().close(); } catch (Exception ignored) {}
            }
        }
    }

    private static String safeRead(Process process, boolean stdout) {
        if (process == null) return "";
        try {
            if (stdout) {
                return IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8).trim();
            }
            return IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            return "";
        }
    }
}
