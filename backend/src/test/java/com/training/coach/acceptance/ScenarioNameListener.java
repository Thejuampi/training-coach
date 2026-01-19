package com.training.coach.acceptance;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom listener to print scenario names to console during test execution.
 * Enable with: cucumber.plugin = com.training.coach.acceptance.ScenarioNameListener
 * Disable by removing from cucumber.plugin configuration.
 */
public class ScenarioNameListener implements Plugin, EventListener {

    private static final Logger log = LoggerFactory.getLogger(ScenarioNameListener.class);

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, this::onTestCaseStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
    }

    private void onTestCaseStarted(TestCaseStarted event) {
        String scenarioName = event.getTestCase().getName();
        String uri = event.getTestCase().getUri().toString();
        log.info("[SCENARIO] {}: {}", uri, scenarioName);
    }

    private void onTestCaseFinished(TestCaseFinished event) {
        String scenarioName = event.getTestCase().getName();
        String status = event.getResult().getStatus().toString().toLowerCase();
        log.info("[SCENARIO COMPLETE] {} - {}", scenarioName, status);
    }
}
