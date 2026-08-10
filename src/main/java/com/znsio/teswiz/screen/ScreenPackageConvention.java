package com.znsio.teswiz.screen;

import java.util.Arrays;

import com.znsio.teswiz.exceptions.InvalidTestDataException;

/**
 * Derives the screen-resolution "root package" and "domain" for a screen contract class from its
 * own package name, instead of assuming every consumer's contracts live under
 * {@code com.znsio.teswiz.screen}. A contract's root package is everything up to and including its
 * first {@code screen} package segment; the domain is whatever comes after it (possibly blank).
 * <p>
 * This lets consumers declare screen contracts under their own namespace
 * (e.g. {@code com.acme.tests.screen.login.LoginScreen}) and still have platform implementations
 * resolved by convention (e.g. {@code com.acme.tests.screen.android.login.LoginScreenAndroid}).
 */
public final class ScreenPackageConvention {
    private static final String SCREEN_SEGMENT = "screen";

    private ScreenPackageConvention() {
    }

    public static String rootPackageOf(String contractPackageName) {
        return split(contractPackageName)[0];
    }

    public static String domainOf(String contractPackageName) {
        return split(contractPackageName)[1];
    }

    private static String[] split(String contractPackageName) {
        String[] segments = contractPackageName.split("\\.");
        int screenIndex = -1;
        for (int index = 0; index < segments.length; index++) {
            if (SCREEN_SEGMENT.equals(segments[index])) {
                screenIndex = index;
                break;
            }
        }
        if (screenIndex == -1) {
            throw new InvalidTestDataException(String.format(
                    "Screen contract package '%s' must contain a package segment named '%s' "
                            + "(e.g. com.example.tests.screen or com.example.tests.screen.<domain>)",
                    contractPackageName, SCREEN_SEGMENT));
        }
        String root = String.join(".", Arrays.copyOfRange(segments, 0, screenIndex + 1));
        String domain = screenIndex + 1 < segments.length
                ? String.join(".", Arrays.copyOfRange(segments, screenIndex + 1, segments.length))
                : "";
        return new String[] { root, domain };
    }
}
