package com.znsio.e2e.listener;

import com.epam.reportportal.cucumber.ScenarioReporter;
import com.epam.reportportal.service.Launch;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.apache.log4j.Logger;
import rp.com.google.common.base.Suppliers;

import java.util.Calendar;

public class CucumberWebScenarioReporterListener extends ScenarioReporter {
    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

    public CucumberWebScenarioReporterListener () throws Exception {
        LOGGER.info("CucumberWebScenarioReporterListener");
    }

    @Override
    protected void startRootItem () {
        this.rootSuiteId =
                Suppliers.memoize(
                        () -> {
                            StartTestItemRQ rq = new StartTestItemRQ();
                            rq.setName("Web Tests");
                            rq.setStartTime(Calendar.getInstance().getTime());
                            rq.setType("SUITE");
                            return ((Launch) this.launch.get()).startTestItem(rq);
                        });
    }
}
