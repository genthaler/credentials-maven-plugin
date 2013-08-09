package com.github.genthaler.credentials;

/*
 * Copyright 2006 The Codehaus
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.junit.Test;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

/**
 * Unit test for CredentialsMojo.
 */
public class CredentialsMojoProperTest extends AbstractMojoTestCase {
	private CredentialsMojo mojo;
	private MavenProjectStub project;
	private Settings settings;
	private Server server;
	private Properties properties;
	private Throwable thrown = null;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		mojo = new CredentialsMojo();
		SecDispatcher securityDispatcher = (SecDispatcher) lookup(
				"org.sonatype.plexus.components.sec.dispatcher.SecDispatcher",
				"default");
		mojo.setSecurityDispatcher(securityDispatcher);
		properties = new Properties();
		project = new MavenProjectStub() {
			@Override
			public Properties getProperties() {
				return properties;
			}
		};
		setVariableValueToObject(mojo, "project", project);
		settings = new Settings();
		mojo.setSettings(settings);
		server = new Server();
		settings.addServer(server);
	}

	@Test
	public void testNoInformation() {
		emptyMojo();
		executeMojo();
		anExceptionIsThrown("MojoExecutionException",
				"At least one of settingsKey and usernameProperty must be set");
	}

	public void emptyMojo() {
	}

	public void setMojoProperty(String property, String value)
			throws IllegalAccessException {
		if ("useSystemProperties".equalsIgnoreCase(property))
			setVariableValueToObject(mojo, property,
					Boolean.parseBoolean(value));
		else
			setVariableValueToObject(mojo, property, value);
	}

	public void setServerProperty(String property, String value)
			throws IllegalAccessException {
		setVariableValueToObject(server, property, value);
	}

	public void setProjectProperty(String property, String value)
			throws IllegalAccessException {
		properties.setProperty(property, value);
	}

	public void setSystemProperty(String property, String value)
			throws IllegalAccessException {
		System.setProperty(property, value);
	}

	public void executeMojo() {
		try {
			mojo.execute();
		} catch (MojoExecutionException e) {
			thrown = e;
		}
	}

	public void assertProjectProperty(String property, String value) {
		assertEquals(value, properties.getProperty(property));
	}

	public void assertSystemProperty(String property, String value) {
		assertEquals(value, System.clearProperty(property));
	}

	public void nothingHappens() {
	}

	public void anExceptionIsThrown(String exceptionClass, String message) {
		assertEquals(exceptionClass, thrown.getClass().getSimpleName());
		assertEquals(message, thrown.getMessage());
	}
}
