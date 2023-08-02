package com.znsio.teswiz.services;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class UnirestService {

    private static final Logger LOGGER = Logger.getLogger(UnirestService.class);
    private static final Unirest unirest = new Unirest();

    private static Unirest getUnirestObj(){
        unirest.config().verifySsl(false);
        return unirest;
    }

    public static HttpResponse<JsonNode> getHttpResponseWithQueryParameter(String completeURLPath, String key, String value) {
        LOGGER.info("Processing GET call with base URL and single query parameter");
        return getUnirestObj().get(completeURLPath).headers(getHeadersWithoutAuthorization()).queryString(key, value).asJson();
    }

    public static HttpResponse<JsonNode> getHttpResponseWithQueryMap(String completeURLPath, HashMap<String, Object> query) {

        LOGGER.info("Processing GET call with base URL and multi query parameters");
        return getUnirestObj().get(completeURLPath).headers(getHeadersWithoutAuthorization()).queryString(query).asJson();
    }

    public static HttpResponse<JsonNode> postHttpRequest(String completeURLPath, Object requestBody) {
        LOGGER.info("Processing post call");
        return getUnirestObj().post(completeURLPath).body(requestBody).headers(getHeadersWithoutAuthorization()).asJson();
    }

    public static HttpResponse<JsonNode> patchHttpRequest(String completeURLPath, Object requestBody) {
        LOGGER.info("Processing patch call");
        return getUnirestObj().patch(completeURLPath).body(requestBody).headers(getHeadersWithoutAuthorization()).asJson();
    }

    public static HttpResponse<JsonNode> deleteHttpRequest(String completeURLPath){
        LOGGER.info("Processing delete call");
        return getUnirestObj().delete(completeURLPath).asJson();
    }
    private static Map<String, String> getHeadersWithoutAuthorization() {
        return new HashMap<>() {{
            put("Accept", "application/json");
            put("content-type", "application/json");
        }};
    }

}
