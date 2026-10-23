package com.znsio.teswiz.web.playwright;

import org.json.JSONObject;

public record PlaywrightWorkerResponse(String requestId, String action, boolean ok, JSONObject payload) {
}
