package com.znsio.teswiz.runner;

import org.json.JSONObject;

public record PlaywrightWorkerResponse(String requestId, String action, boolean ok, JSONObject payload) {
}
