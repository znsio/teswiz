package com.znsio.teswiz.services;

import com.sun.net.httpserver.HttpServer;
import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestAssuredServiceTest {

    private static final String DISABLE_ENVIRONMENT_ISSUE_FILTER = "DISABLE_ENVIRONMENT_ISSUE_FILTER";
    private static HttpServer server;
    private static String serviceUnavailableUrl;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/unavailable", exchange -> {
            byte[] body = "Service Unavailable".getBytes();
            exchange.sendResponseHeaders(503, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        serviceUnavailableUrl = "http://localhost:" + server.getAddress().getPort() + "/unavailable";
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @AfterEach
    void clearOverride() {
        System.clearProperty(DISABLE_ENVIRONMENT_ISSUE_FILTER);
    }

    @Test
    void throwsEnvironmentSetupExceptionByDefaultOn503() {
        assertThatThrownBy(() -> RestAssuredService.getHttpResponse(serviceUnavailableUrl))
                .isInstanceOf(EnvironmentSetupException.class)
                .hasMessageContaining("503");
    }

    @Test
    void doesNotThrowWhenFilterExplicitlyDisabled() {
        System.setProperty(DISABLE_ENVIRONMENT_ISSUE_FILTER, "true");

        Response response = RestAssuredService.getHttpResponse(serviceUnavailableUrl);

        assertThat(response.getStatusCode()).isEqualTo(503);
    }
}
