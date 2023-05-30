package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.search.SearchBL;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

public class SearchSteps {
    private static final Logger LOGGER = Logger.getLogger(SearchSteps.class.getName());
    private final TestExecutionContext context;

    public SearchSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @When("{string} searches for {string}")
    public void searchesFor(String userPersona, String searchFor) {
        Platform onPlatform = Runner.getPlatformForUser(userPersona);
        new SearchBL(userPersona, onPlatform).searchFor(searchFor);
    }
}
