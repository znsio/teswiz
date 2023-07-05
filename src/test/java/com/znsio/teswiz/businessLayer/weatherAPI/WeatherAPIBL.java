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

    public JSONObject getCurrentWeatherJSON() {
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

        return jsonResponse.getBody().getObject().getJSONObject("current_weather");
    }

    public void verifyCurrentWeather(JSONObject jsonResponse) {
        LOGGER.info("Verifying weather is in range 0-55 C");
        assertThat(((int) jsonResponse.getDouble("temperature"))).as("Weather value incorrect!")
                .isBetween(0,55);
    }
}
