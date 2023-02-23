package com.znsio.sample.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Drivers;
import com.znsio.sample.e2e.businessLayer.search.SearchBL;
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
        Platform onPlatform = Drivers.getPlatformForUser(userPersona);
        new SearchBL(userPersona, onPlatform).searchFor(searchFor);
    }
}
