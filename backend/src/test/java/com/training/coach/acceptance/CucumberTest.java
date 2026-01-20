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
@ConfigurationParameter(key = "cucumber.glue", value = "com.training.coach.acceptance")
@ConfigurationParameter(key = "cucumber.filter.tags", value = "not @wip")
@ConfigurationParameter(key = "cucumber.plugin", value = "com.training.coach.acceptance.ScenarioNameListener")
public class CucumberTest {}
