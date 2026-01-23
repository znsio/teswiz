package com.znsio.teswiz.tools;

public final class OsUtils {
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String USER_DIRECTORY = System.getProperty("user.dir");
    private static final String USER_NAME = System.getProperty("user.name");

    public static String getOsName() { return OS; }
    public static String getUserDirectory() { return USER_DIRECTORY; }
    public static String getUserName() { return USER_NAME; }
    public static boolean isWindows() { return OS.contains("win"); }
    public static boolean isMac() { return OS.contains("mac"); }
    public static boolean isLinux() { return OS.contains("nux") || OS.contains("nix"); }

    private OsUtils() {}
}
