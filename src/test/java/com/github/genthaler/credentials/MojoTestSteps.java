package com.github.genthaler.credentials;

/*
 * #%L
 * Credentials Maven Plugin
 * %%
 * Copyright (C) 2013 - 2014 Günther Enthaler
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

import junit.framework.AssertionFailedError;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import com.github.genthaler.credentials.AbstractCredentialsMojo;
import com.github.genthaler.credentials.SetAllMojo;
import com.github.genthaler.credentials.SetMojo;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Unit test for SetMojo.
 */
public class MojoTestSteps extends AbstractMojoTestCase {
	private AbstractCredentialsMojo mojo;
	private MavenProjectStub project;
	private Settings settings;
	private Properties properties;
	private Throwable thrown = null;
	private List<String> systemProperties;

	@Before(value = { "@set" }, order = 1)
	public void setUpSetMojo() {
		mojo = new SetMojo();
	}

	@Before(value = { "@set-all" }, order = 1)
	public void setUpSetAllMojo() {
		mojo = new SetAllMojo();
	}

	@Override
	@Before(order = 2)
	public void setUp() throws Exception {
		super.setUp();
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
		setVariableValueToObject(mojo, "settings", settings);
		systemProperties = new ArrayList<String>();

		mojo.setLog(new SystemStreamLog() {
			@Override
			public boolean isDebugEnabled() {
				return true;
			}
		});
	}

	@Given("^a (set.*) Plugin")
	public void emptyMojo(String goal) {
	}

	@Given("^the Plugin (.*) property is (.*)$")
	public void setMojoProperty(String property, String value)
			throws IllegalAccessException {
		if ("useSystemProperties".equalsIgnoreCase(property))
			setVariableValueToObject(mojo, property,
					Boolean.parseBoolean(value));
		else
			setVariableValueToObject(mojo, property, value);
	}

	@Given("^Server (.*)'s (.*) is (.*)$")
	public void setServerProperty(String serverId, String property, String value)
			throws IllegalAccessException {
		Server server = settings.getServer(serverId);
		if (server == null) {
			server = new Server();
			server.setId(serverId);
			settings.addServer(server);
		}
		setVariableValueToObject(server, property, value);
	}

	@Given("^an empty Server with id (.*)$")
	public void addEmptyServer(String serverId) throws IllegalAccessException {
		Server server = settings.getServer(serverId);
		if (server != null)
			throw new AssertionFailedError(String.format(
					"There's already a server with ID %s", serverId));
		server = new Server();
		settings.addServer(server);
		setVariableValueToObject(server, "id", serverId);
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

	@When("^the (.*) Goal is executed")
	public void executeMojo(String goal) {
		try {
			mojo.execute();
		} catch (MojoExecutionException e) {
			thrown = e;
		} catch (MojoFailureException e) {
			thrown = e;
		}
	}

	@Then("^there should be a Project (.*) property with value (.*)$")
	public void assertProjectProperty(String property, String value) {
		if ("\"\"".equals(value))
			value = "";
		assertEquals(value, properties.getProperty(property));
	}

	@Then("^there should be a System (.*) property with value (.*)$")
	public void assertSystemProperty(String property, String value) {
		assertEquals(value, System.getProperty(property));
	}

	@Then("^there should be an error message '(.*)'$")
	public void anExceptionIsThrown(String message) {
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
