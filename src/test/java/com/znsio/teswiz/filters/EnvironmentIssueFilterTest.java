package com.znsio.teswiz.filters;

import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnvironmentIssueFilterTest {

    private final EnvironmentIssueFilter filter = new EnvironmentIssueFilter();
    private FilterableRequestSpecification requestSpec;
    private FilterableResponseSpecification responseSpec;
    private FilterContext ctx;
    private Response response;

    @BeforeEach
    void setup() {
        requestSpec = mock(FilterableRequestSpecification.class);
        responseSpec = mock(FilterableResponseSpecification.class);
        ctx = mock(FilterContext.class);
        response = mock(Response.class);

        when(requestSpec.getURI()).thenReturn("https://example.com/health");
        when(ctx.next(any(), any())).thenReturn(response);
    }

    @ParameterizedTest
    @ValueSource(ints = {502, 503, 504})
    void throwsEnvironmentSetupExceptionForGatewayStatusCodes(int statusCode) {
        when(response.getStatusCode()).thenReturn(statusCode);
        when(response.getBody()).thenReturn(mock(io.restassured.response.ResponseBody.class));
        when(response.getBody().asString()).thenReturn("Bad Gateway");

        assertThatThrownBy(() -> filter.filter(requestSpec, responseSpec, ctx))
                .isInstanceOf(EnvironmentSetupException.class)
                .hasMessageContaining(String.valueOf(statusCode))
                .hasMessageContaining("https://example.com/health")
                .hasMessageContaining("Bad Gateway");
    }

    @Test
    void doesNotDuplicateBaseUriInExceptionMessage() {
        when(response.getStatusCode()).thenReturn(502);
        when(response.getBody()).thenReturn(mock(io.restassured.response.ResponseBody.class));
        when(response.getBody().asString()).thenReturn("Bad Gateway");

        assertThatThrownBy(() -> filter.filter(requestSpec, responseSpec, ctx))
                .hasMessageNotContaining("https://example.comhttps://example.com");
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 201, 400, 401, 404, 500})
    void passesThroughResponseForNonGatewayStatusCodes(int statusCode) {
        when(response.getStatusCode()).thenReturn(statusCode);

        Response actual = filter.filter(requestSpec, responseSpec, ctx);

        assertThat(actual).isSameAs(response);
    }

    @Test
    void truncatesLongResponseBodyInExceptionMessage() {
        String longBody = "x".repeat(500);
        when(response.getStatusCode()).thenReturn(503);
        when(response.getBody()).thenReturn(mock(io.restassured.response.ResponseBody.class));
        when(response.getBody().asString()).thenReturn(longBody);

        assertThatThrownBy(() -> filter.filter(requestSpec, responseSpec, ctx))
                .isInstanceOf(EnvironmentSetupException.class)
                .hasMessageContaining("x".repeat(200) + "...")
                .hasMessageNotContaining(longBody);
    }
}
