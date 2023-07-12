package com.znsio.teswiz.businessLayer.weatherAPI;

import com.znsio.teswiz.runner.Runner;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.apache.log4j.Logger;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.array;

public class WeatherAPIBL {
    private static final Logger LOGGER = Logger.getLogger(WeatherAPIBL.class.getName());
    private final Map<String, Object> testData = Runner.getTestDataAsMap("Weather_API");
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

        assertThat(jsonResponse.getStatus()).as("API status code incorrect!")
                .isEqualTo(200);
        return jsonResponse.getBody().getObject().getJSONObject("current_weather");
    }

    public WeatherAPIBL verifyCurrentTemperature(JSONObject jsonResponse, int lowerLimit, int upperLimit) {
        LOGGER.info("Verifying weather is in range "+lowerLimit+" and "+upperLimit+" C");
        assertThat(((int) jsonResponse.getDouble("temperature"))).as("Temperature value incorrect!")
                .isBetween(lowerLimit,upperLimit);
        return this;
    }

    public JSONObject getForecastForInvalidDays() {
        String days = testData.get("days").toString();
        String latitude = testData.get("latitude").toString();
        String longitude = testData.get("longitude").toString();
        String hourly = testData.get("hourly").toString();
        LOGGER.info("Getting temperature forecast for days: "+days);
        HttpResponse<JsonNode> jsonResponse
                = Unirest.get(base_URL)
                .header("accept", "application/json")
                .queryString("latitude", latitude)
                .queryString("longitude",longitude)
                .queryString("hourly",hourly)
                .queryString("forecast_days",days)
                .asJson();

        assertThat(jsonResponse.getStatus()).as("API status code incorrect!")
                .isEqualTo(400);
        return jsonResponse.getBody().getObject();
    }

    public WeatherAPIBL verifyErrorForInvalidForecastDays(JSONObject jsonObject, String errorMessage) {
        String jsonError = jsonObject.get("reason").toString();
        LOGGER.info("Verifying error message for invalid forecast days");
        assertThat(jsonError.contains(errorMessage))
                .as("Incorrect error message!").isTrue();
        return this;
    }

    public JSONObject getCurrentWeatherJSON(String latitude, String longitude) {
        LOGGER.info("Getting current weather data for given location coordinates");
        HttpResponse<JsonNode> jsonResponse
                = Unirest.get(base_URL)
                .header("accept", "application/json")
                .queryString("latitude", latitude)
                .queryString("longitude",longitude)
                .queryString("current_weather",true)
                .asJson();

        assertThat(jsonResponse.getStatus()).as("API status code incorrect!")
                .isEqualTo(200);
        return jsonResponse.getBody().getObject().getJSONObject("current_weather");
    }

    public WeatherAPIBL verifyCurrentWindSpeed(JSONObject jsonResponse, int lowerLimit, int upperLimit) {
        LOGGER.info("Verifying wind speed is in range "+lowerLimit+" and "+upperLimit);
        assertThat(((int) jsonResponse.getDouble("windspeed"))).as("Wind speed value incorrect!")
                .isBetween(lowerLimit,upperLimit);
        return this;
    }

    public JSONObject getLocationCoordinatesFor(String city) {
        LOGGER.info("Getting coordinates for city "+city);
        String geocode_url = testData.get("geocode_url").toString();

        HttpResponse<JsonNode> jsonResponse
                = Unirest.get(geocode_url)
                .header("accept", "application/json")
                .queryString("q", city)
                .asJson();

        assertThat(jsonResponse.getStatus()).as("API status code incorrect!")
                .isEqualTo(200);
        return jsonResponse.getBody().getArray().getJSONObject(0);
    }

    public String getLatitudeFromJSON(JSONObject jsonObject) {
        LOGGER.info("Fetch latitude value from json response");
        assertThat(jsonObject.has("lat"))
                .as("Latitude in not available in JSON response!")
                .isTrue();
        return jsonObject.getString("lat");
    }

    public String getLongitudeFromJSON(JSONObject jsonObject) {
        LOGGER.info("Fetch longitude value from json response");
        assertThat(jsonObject.has("lon"))
                .as("Longitude in not available in JSON response!")
                .isTrue();
        return jsonObject.getString("lon");
    }

    public WeatherAPIBL verifyCurrentWindDirection(JSONObject jsonObject, int maxWindDirection) {
        LOGGER.info("Verifying maximum wind direction is less than: "+maxWindDirection);
        assertThat((Double) jsonObject.get("winddirection"))
                .as("Wind direction above maximum limit!").isLessThan(maxWindDirection);
        return this;
    }
}
