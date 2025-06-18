package com.znsio.teswiz.runner.atd;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Api {
    private static final Logger LOGGER = LogManager.getLogger(Api.class.getName());

    public String getResponse(String url) {
        String body;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            body = response.body().string();
            response.close();

        } catch (Exception e) {
            throw new RuntimeException("unable to call device farm endpoints " + e.getMessage());
        }
        return body;
    }
}
