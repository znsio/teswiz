package com.znsio.teswiz.businessLayer.weatherAPI;

import com.znsio.teswiz.businessLayer.indigo.IndigoBL;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.apache.log4j.Logger;
import static org.assertj.core.api.Assertions.assertThat;

public class WeatherAPIBL {
    private static final Logger LOGGER = Logger.getLogger(IndigoBL.class.getName());
    private final String base_URL="https://api.open-meteo.com/v1/forecast?";

    public JSONObject getCurrentWeatherJSON(String latitude, String longitude) {
        LOGGER.info("Getting current weather data for given location coordinates");
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
