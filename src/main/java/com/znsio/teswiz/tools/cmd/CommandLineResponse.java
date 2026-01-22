package com.znsio.teswiz.tools.cmd;

public class CommandLineResponse {

    private int exitCode;
    private String stdOut;
    private String errOut;

    private boolean timedOut;
    private long durationMillis;
    private String command;

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getStdOut() {
        return stdOut;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public String getErrOut() {
        return errOut;
    }

    public void setErrOut(String errOut) {
        this.errOut = errOut;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public void setTimedOut(boolean timedOut) {
        this.timedOut = timedOut;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "CommandLineResponse [exitCode=" + exitCode +
               ", timedOut=" + timedOut +
               ", durationMillis=" + durationMillis +
               ", command=" + command +
               ", stdOut=" + stdOut +
               ", errOut=" + errOut + "]";
    }
}
