package com.znsio.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.businessLayer.CalculatorBL;
import com.znsio.e2e.businessLayer.SearchBL;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.e2e.tools.Drivers;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

import static com.znsio.e2e.tools.Wait.waitFor;

public class SearchSteps {
    private static final Logger LOGGER = Logger.getLogger(SearchSteps.class.getName());
    private final TestExecutionContext context;
    private final Drivers allDrivers;

    public SearchSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
        allDrivers = (Drivers) context.getTestState(SAMPLE_TEST_CONTEXT.ALL_DRIVERS);
        LOGGER.info("allDrivers: " + (null == allDrivers));
    }

    @When("{string} searches for {string}")
    public void searchesFor(String userPersona, String searchFor) {
        Platform onPlatform = allDrivers.getPlatformForUser(userPersona);
        new SearchBL(userPersona, onPlatform).searchFor(searchFor);
    }
}
