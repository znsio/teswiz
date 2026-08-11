package com.znsio.teswiz.businessLayer.restUser;

import com.znsio.teswiz.businessLayer.weatherAPI.WeatherAPIBL;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.services.RestAssuredService;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPlaceHolderBL {

    private static final Logger LOGGER = LogManager.getLogger(WeatherAPIBL.class.getName());
    private final Map<String, Object> testData = Runner.getTestDataAsMap("RestUser_API");
    private final String base_URL = testData.get("url").toString();

    public JsonPlaceHolderBL createPost() {
        Object jsonBody = testData.get("postBody");
        LOGGER.info("Creating a post");
        Response jsonResponse = RestAssuredService.postHttpRequest(base_URL,jsonBody);
        assertThat(jsonResponse.getStatusCode()).as("Received API status code for POST method incorrect!")
                .isEqualTo(201);
        LOGGER.info("Verifying post is created successfully");
        assertThat(jsonResponse.jsonPath().getInt("id")).as("API status code for POST method incorrect!")
                .isEqualTo(101);
        return this;
    }

    public JSONObject updatePost() {
        LOGGER.info("Updating a post");
        Object jsonBody = testData.get("patchBody");
        Response jsonResponse = RestAssuredService.patchHttpRequest(base_URL+"/1", jsonBody);
        assertThat(jsonResponse.getStatusCode()).as("Received API status code for PATCH method incorrect!")
                .isEqualTo(200);
        return new JSONObject(jsonResponse.getBody().asString());
    }

    public JsonPlaceHolderBL verifyPostUpdatedSuccessfully(JSONObject jsonResponse) {
        LOGGER.info("Verifying post is updated successfully");
        String updatedTitle = testData.get("updatedTitle").toString();
        assertThat(jsonResponse.get("title")).as("Updated Title not matched!")
                .isEqualTo(updatedTitle);
        return this;
    }

    public int deletePost() {
        LOGGER.info("Verifying post is deleted successfully");
        Response jsonResponse = RestAssuredService.deleteHttpRequest(base_URL+"/1");
        assertThat(jsonResponse.getStatusCode()).as("Received API status code for Delete method incorrect!")
                .isEqualTo(200);
        return jsonResponse.getStatusCode();
    }

    public JsonPlaceHolderBL verifyIfPostDeleted(int status){
        assertThat(status).as("Received API status code for Delete method incorrect!")
                .isEqualTo(200);
        return this;
    }
}
