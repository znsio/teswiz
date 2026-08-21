package com.znsio.teswiz.filters;

import com.znsio.teswiz.exceptions.EnvironmentSetupException;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * RestAssured filter that detects environment issues (502, 503, 504) and throws
 * EnvironmentSetupException immediately — before the response reaches BL/Steps.
 * <p>
 * A 502/503/504 is never a valid test expectation. It always means the target
 * service is unavailable. This filter ensures such failures are categorized as
 * environment issues, not test logic failures.
 */
public class EnvironmentIssueFilter implements Filter {

    private static final int HTTP_BAD_GATEWAY = 502;
    private static final int HTTP_SERVICE_UNAVAILABLE = 503;
    private static final int HTTP_GATEWAY_TIMEOUT = 504;

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec);
        int status = response.getStatusCode();

        if (status == HTTP_BAD_GATEWAY || status == HTTP_SERVICE_UNAVAILABLE || status == HTTP_GATEWAY_TIMEOUT) {
            String url = requestSpec.getURI();
            String body = response.getBody().asString();
            String truncatedBody = body.length() <= 200 ? body : body.substring(0, 200) + "...";

            throw new EnvironmentSetupException(String.format(
                    "Environment issue: service at '%s' returned HTTP %d. " +
                    "This is not a test failure — the service is unavailable. Body: %s",
                    url, status, truncatedBody));
        }

        return response;
    }
}
