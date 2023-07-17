package com.znsio.teswiz.businessLayer.restUser;

import com.znsio.teswiz.businessLayer.weatherAPI.WeatherAPIBL;
import com.znsio.teswiz.runner.Runner;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.apache.log4j.Logger;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

public class RestUserBL {

    private static final Logger LOGGER = Logger.getLogger(WeatherAPIBL.class.getName());
    private final Map<String, Object> testData = Runner.getTestDataAsMap("RestUser_API");
    private final String base_URL = testData.get("url").toString();

    public JSONObject createPost() {
        Object jsonBody = testData.get("postBody");
        LOGGER.info("Creating a post");
        HttpResponse<JsonNode> jsonResponse = Unirest.post(base_URL)
                .header("Content-type", "application/json; charset=UTF-8")
                .body(jsonBody)
                .asJson();

        assertThat(jsonResponse.getStatus()).as("Received API status code for POST method incorrect!")
                .isEqualTo(201);
        return jsonResponse.getBody().getObject();
    }

    public RestUserBL verifyPostCreatedSuccessfully(JSONObject jsonResponse) {
        LOGGER.info("Verifying post is created successfully");
        assertThat(jsonResponse.get("id")).as("API status code for POST method incorrect!")
                .isEqualTo(101);
        return this;
    }

    public JSONObject updatePost() {
        LOGGER.info("Updating a post");
        Object patchBody = testData.get("patchBody");
        HttpResponse<JsonNode> jsonResponse = Unirest.patch(base_URL+"/1")
                .header("Content-type", "application/json; charset=UTF-8")
                .body(patchBody)
                .asJson();

        assertThat(jsonResponse.getStatus()).as("Received API status code for PATCH method incorrect!")
                .isEqualTo(200);
        return jsonResponse.getBody().getObject();
    }

    public RestUserBL verifyPostUpdatedSuccessfully(JSONObject jsonResponse) {
        LOGGER.info("Verifying post is updated successfully");
        String updatedTitle = testData.get("updatedTitle").toString();
        assertThat(jsonResponse.get("title")).as("API status code for PATCH method incorrect!")
                .isEqualTo(updatedTitle);
        return this;
    }

    public RestUserBL deletePost() {
        LOGGER.info("Verifying post is deleted successfully");
        HttpResponse<JsonNode> jsonResponse = Unirest.delete(base_URL+"/1").asJson();
        assertThat(jsonResponse.getStatus()).as("Received API status code for PATCH method incorrect!")
                .isEqualTo(200);
        return this;
    }
}
