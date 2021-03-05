package com.znsio.e2e.listener;

import com.epam.reportportal.cucumber.ScenarioReporter;
import com.epam.reportportal.service.Launch;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import rp.com.google.common.base.Suppliers;

import java.util.Calendar;

public class CucumberWebScenarioReporterListener extends ScenarioReporter {

    public CucumberWebScenarioReporterListener () throws Exception {
        System.out.println("CucumberWebScenarioReporterListener");
    }

    @Override
    protected void startRootItem () {
        this.rootSuiteId =
                Suppliers.memoize(
                        () -> {
                            StartTestItemRQ rq = new StartTestItemRQ();
                            rq.setName("My Web Tests");
                            rq.setStartTime(Calendar.getInstance().getTime());
                            rq.setType("SUITE");
                            return ((Launch) this.launch.get()).startTestItem(rq);
                        });
    }
}
