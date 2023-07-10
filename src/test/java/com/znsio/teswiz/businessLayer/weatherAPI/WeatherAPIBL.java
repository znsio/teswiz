package com.znsio.teswiz.businessLayer.weatherAPI;

import com.znsio.teswiz.runner.Runner;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.apache.log4j.Logger;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

public class WeatherAPIBL {
    private static final Logger LOGGER = Logger.getLogger(WeatherAPIBL.class.getName());
    private final Map<String, Object> testData = Runner.getTestDataAsMap("API");
    private final String base_URL = testData.get("url").toString();

    public HttpResponse<JsonNode> getCurrentWeatherJSON() {
        LOGGER.info("Getting current weather data for given location coordinates");
        String latitude = testData.get("latitude").toString();
        String longitude = testData.get("longitude").toString();
        HttpResponse<JsonNode> jsonResponse
                = Unirest.get(base_URL)
                .header("accept", "application/json")
                .queryString("latitude", latitude)
                .queryString("longitude",longitude)
                .queryString("current_weather",true)
                .asJson();

        assertThat(jsonResponse.getStatus()).as("API status code incorrect!")
                .isEqualTo(200);
        return jsonResponse;
    }

    public JSONObject getValueForKey(HttpResponse<JsonNode> jsonResponse, String key) {
        assertThat(jsonResponse.getBody().getObject().has(key))
                .as("Key: "+key+" not found in JSON response!").isTrue();
        return jsonResponse.getBody().getObject().getJSONObject(key);
    }

    public void verifyCurrentTemperature(JSONObject jsonResponse, int lowerLimit, int upperLimit) {
        LOGGER.info("Verifying weather is in range 0-55 C");
        assertThat(((int) jsonResponse.getDouble("temperature"))).as("Weather value incorrect!")
                .isBetween(lowerLimit,upperLimit);
    }

    public void verifyKeyValueInResponse(JSONObject jsonResponse, String key, int value) {
        LOGGER.info("Verifying "+key +" is equals : "+ value);
        assertThat(jsonResponse.getInt(key)).as(key+" not equals "+value).isEqualTo(value);
    }
}
