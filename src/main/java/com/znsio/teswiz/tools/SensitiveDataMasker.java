package com.znsio.teswiz.tools;

import java.util.regex.Pattern;

public final class SensitiveDataMasker {
    private static final String MASK = "***";

    private static final Pattern URL_CREDENTIALS = Pattern.compile(
            "(?i)(https?://)([^:/\\s]+):([^@\\s]+)@");
    private static final Pattern CURL_USER_CREDENTIALS = Pattern.compile(
            "(?i)(-u\\s+['\"]?)([^:\\s'\"\\\\]+):([^\\s'\"\\\\]+)(['\"]?)");
    private static final Pattern AUTHORIZATION_BEARER = Pattern.compile(
            "(?i)(authorization\\s*[:=]\\s*bearer\\s+)([^,\\s]+)");
    private static final Pattern JSON_SENSITIVE_KEY_VALUE = Pattern.compile(
            "(?i)(\"(?:access[_-]?key|api[_-]?key|auth[_-]?token|token|password|passwd|secret|"
            + "client[_-]?secret|cloud[_-]?key|cloud[_-]?username|pcloudy_apikey|"
            + "pcloudy_username|authorization|userName)\"\\s*:\\s*\")([^\"]+)(\")");
    private static final Pattern TEXT_SENSITIVE_KEY_VALUE = Pattern.compile(
            "(?i)\\b(access[_-]?key|api[_-]?key|auth[_-]?token|token|password|passwd|secret|"
            + "client[_-]?secret|cloud[_-]?key|cloud[_-]?username|pcloudy_apikey|"
            + "pcloudy_username|authorization|userName)\\b\\s*[:=]\\s*([^,\\s}\\]]+)");

    private SensitiveDataMasker() {}

    public static String mask(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String masked = value;
        masked = URL_CREDENTIALS.matcher(masked).replaceAll("$1" + MASK + ":" + MASK + "@");
        masked = CURL_USER_CREDENTIALS.matcher(masked).replaceAll("$1" + MASK + ":" + MASK + "$4");
        masked = AUTHORIZATION_BEARER.matcher(masked).replaceAll("$1" + MASK);
        masked = JSON_SENSITIVE_KEY_VALUE.matcher(masked).replaceAll("$1" + MASK + "$3");
        masked = TEXT_SENSITIVE_KEY_VALUE.matcher(masked).replaceAll("$1=" + MASK);
        return masked;
    }
}
