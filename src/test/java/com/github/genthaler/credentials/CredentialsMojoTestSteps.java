package com.github.genthaler.credentials;

/*
 * #%L
 * Credentials Maven Plugin
 * %%
 * Copyright (C) 2013 Günther Enthaler
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Unit test for CredentialsMojo.
 */
public class CredentialsMojoTestSteps extends AbstractMojoTestCase {
	private CredentialsMojo mojo;
	private MavenProjectStub project;
	private Settings settings;
	private Server server;
	private Properties properties;
	private Throwable thrown = null;
	private List<String> systemProperties;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		mojo = new CredentialsMojo();
		SecDispatcher securityDispatcher = (SecDispatcher) lookup(
				"org.sonatype.plexus.components.sec.dispatcher.SecDispatcher",
				"default");
		setVariableValueToObject(mojo, "securityDispatcher", securityDispatcher);
		securityDispatcher.decrypt("");
		properties = new Properties();
		project = new MavenProjectStub() {
			@Override
			public Properties getProperties() {
				return properties;
			}
		};
		setVariableValueToObject(mojo, "project", project);

		settings = new Settings();
		server = new Server();
		settings.addServer(server);
		setVariableValueToObject(mojo, "settings", settings);
		systemProperties = new ArrayList<String>();

		mojo.setLog(new SystemStreamLog() {
			@Override
			public boolean isDebugEnabled() {
				return true;
			}
		});
	}

	@Given("^an empty Mojo$")
	public void emptyMojo() {
	}

	@Given("^the Mojo (.*) property is (.*)$")
	public void setMojoProperty(String property, String value)
			throws IllegalAccessException {
		if ("useSystemProperties".equalsIgnoreCase(property))
			setVariableValueToObject(mojo, property,
					Boolean.parseBoolean(value));
		else
			setVariableValueToObject(mojo, property, value);
	}

	@Given("^the Server (.*) property is (.*)$")
	public void setServerProperty(String property, String value)
			throws IllegalAccessException {
		setVariableValueToObject(server, property, value);
	}

	@Given("^the Project (.*) property is (.*)$")
	public void setProjectProperty(String property, String value)
			throws IllegalAccessException {
		properties.setProperty(property, value);
	}

	@Given("^the System (.*) property is (.*)$")
	public void setSystemProperty(String property, String value)
			throws IllegalAccessException {
		systemProperties.add(property);
		System.setProperty(property, value);
	}

	@When("^the Mojo is executed$")
	public void executeMojo() {
		try {
			mojo.execute();
		} catch (MojoExecutionException e) {
			thrown = e;
		}
	}

	@Then("^the Project should have a (.*) property with value (.*)$")
	public void assertProjectProperty(String property, String value) {
		if ("\"\"".equals(value))
			value = "";
		assertEquals(value, properties.getProperty(property));
	}

	@Then("^the System should have a (.*) property with value (.*)$")
	public void assertSystemProperty(String property, String value) {
		assertEquals(value, System.getProperty(property));
	}

	@Then("^a (.*) should be thrown with the message '(.*)'$")
	public void anExceptionIsThrown(String exceptionClass, String message) {
		assertEquals(exceptionClass, thrown.getClass().getSimpleName());
		assertEquals(message, thrown.getMessage());
	}

	@After
	public void after() {
		for (String property : systemProperties) {
			System.clearProperty(property);
		}
		systemProperties.clear();
	}
}
