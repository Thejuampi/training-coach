package com.training.coach.acceptance;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.ConfigurationParameter;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/plan-lifecycle.feature")
@SelectClasspathResource("features/auth.feature")
@SelectClasspathResource("features/compliance-progress.feature")
@SelectClasspathResource("features/coach.feature")
@SelectClasspathResource("features/safety-and-guardrails.feature")
@SelectClasspathResource("features/seiler-intensity.feature")
@SelectClasspathResource("features/athlete.feature")
@SelectClasspathResource("features/admin.feature")
@SelectClasspathResource("features/data-retention-consent.feature")
@SelectClasspathResource("features/events-and-goals.feature")
@SelectClasspathResource("features/multi-platform-reconciliation.feature")
@SelectClasspathResource("features/notifications.feature")
@SelectClasspathResource("features/reports-and-exports.feature")
@SelectClasspathResource("features/system.feature")
@SelectClasspathResource("features/ui-views.feature")
@SelectClasspathResource("features/use-cases.feature")
@SelectClasspathResource("features/workout-execution.feature")
@SelectClasspathResource("features/workout-library.feature")
@SelectClasspathResource("features/zones-and-testing.feature")
@SelectClasspathResource("features/availability-and-calendar.feature")
@SelectClasspathResource("features/plan-adjustments.feature")
@ConfigurationParameter(key = "cucumber.glue", value = "com.training.coach.acceptance")
@ConfigurationParameter(key = "cucumber.filter.tags", value = "not @wip")
@ConfigurationParameter(key = "cucumber.plugin", value = "com.training.coach.acceptance.ScenarioNameListener")
public class CucumberTest {}
