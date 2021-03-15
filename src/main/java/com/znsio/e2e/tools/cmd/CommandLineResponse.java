package com.znsio.e2e.tools.cmd;

public class CommandLineResponse {

    private int exitCode;
    private String stdOut;
    private String errOut;

    public int getExitCode () {
        return exitCode;
    }

    public void setExitCode (int exitCode) {
        this.exitCode = exitCode;
    }

    public String getStdOut () {
        return stdOut;
    }

    public void setStdOut (String stdOut) {
        this.stdOut = stdOut;
    }

    public String getErrOut () {
        return errOut;
    }

    public void setErrOut (String errOut) {
        this.errOut = errOut;
    }

    @Override
    public String toString () {
        return "CommandLineResponse [exitCode=" + exitCode + ", stdOut=" + stdOut + ", errOut=" + errOut + "]";
    }
}
