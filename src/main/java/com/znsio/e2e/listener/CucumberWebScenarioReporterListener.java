package com.znsio.e2e.listener;

import com.epam.reportportal.cucumber.ScenarioReporter;
import com.epam.reportportal.utils.MemoizingSupplier;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.apache.log4j.Logger;

import java.util.Calendar;

public class CucumberWebScenarioReporterListener extends ScenarioReporter {
    private static final Logger LOGGER = Logger.getLogger(CucumberWebScenarioReporterListener.class.getName());
    private static final String DUMMY_ROOT_SUITE_NAME = "End-2-End Tests";
    private static final String RP_STORY_TYPE = "SUITE";

    public CucumberWebScenarioReporterListener () throws Exception {
        LOGGER.info("CucumberWebScenarioReporterListener");
    }

    @Override
    protected void startRootItem () {
        this.rootSuiteId =
                rootSuiteId = new MemoizingSupplier<>(() -> {
                    StartTestItemRQ rq = new StartTestItemRQ();
                    rq.setName(DUMMY_ROOT_SUITE_NAME);
                    rq.setStartTime(Calendar.getInstance().getTime());
                    rq.setType(RP_STORY_TYPE);
                    return launch.get().startTestItem(rq);
                });
    }
}
