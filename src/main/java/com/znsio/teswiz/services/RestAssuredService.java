package com.znsio.teswiz.services;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class RestAssuredService {

    private static final Logger LOGGER = LogManager.getLogger(RestAssuredService.class);

    private static RequestSpecification getRequestSpec() {
        return RestAssured.given().relaxedHTTPSValidation().headers(getHeadersWithoutAuthorization());
    }

    public static Response getHttpResponse(String completeURLPath) {
        LOGGER.info("Processing GET call with base URL");
        return getRequestSpec().get(completeURLPath);
    }

    public static Response getHttpResponseWithQueryParameter(String completeURLPath, String key, String value) {
        LOGGER.info("Processing GET call with base URL and single query parameter");
        return getRequestSpec().queryParam(key, value).get(stripTrailingQuestionMark(completeURLPath));
    }

    public static Response getHttpResponseWithQueryMap(String completeURLPath, HashMap<String, Object> query) {
        LOGGER.info("Processing GET call with base URL and multi query parameters");
        return getRequestSpec().queryParams(query).get(stripTrailingQuestionMark(completeURLPath));
    }

    private static String stripTrailingQuestionMark(String completeURLPath) {
        return completeURLPath.endsWith("?")
                ? completeURLPath.substring(0, completeURLPath.length() - 1)
                : completeURLPath;
    }

    public static Response postHttpRequest(String completeURLPath, Object requestBody) {
        LOGGER.info("Processing post call");
        return getRequestSpec().body(requestBody).post(completeURLPath);
    }

    public static Response patchHttpRequest(String completeURLPath, Object requestBody) {
        LOGGER.info("Processing patch call");
        return getRequestSpec().body(requestBody).patch(completeURLPath);
    }

    public static Response deleteHttpRequest(String completeURLPath) {
        LOGGER.info("Processing delete call");
        return getRequestSpec().delete(completeURLPath);
    }

    private static Map<String, String> getHeadersWithoutAuthorization() {
        return new HashMap<>() {{
            put("Accept", "application/json");
            put("content-type", "application/json");
        }};
    }

}
