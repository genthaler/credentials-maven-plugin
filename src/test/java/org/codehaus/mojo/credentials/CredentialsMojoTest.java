package org.codehaus.mojo.credentials;

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
import org.junit.Before;
import org.junit.Test;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

/**
 * Unit test for simple CredentialsMojo.
 */
public class CredentialsMojoTest extends AbstractMojoTestCase {
	private static final String PASSWORD_PROPERTY = "passwordProperty";

	private static final String USERNAME_PROPERTY = "usernameProperty";

	private static final String KEY = "somekey";

	private static final String PASSWORD = "password";

	private static final String USERNAME = "username";

	private CredentialsMojo mojo;

	private Properties p;

	private MavenProjectStub project;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		mojo = new CredentialsMojo();

		SecDispatcher securityDispatcher = (SecDispatcher) lookup(
				"org.sonatype.plexus.components.sec.dispatcher.SecDispatcher",
				"default");
		mojo.setSecurityDispatcher(securityDispatcher);

		project = new MavenProjectStub();
		setVariableValueToObject(mojo, "project", project);
	}

	@Test
	public void testDefaultUsernamePassword() throws MojoExecutionException {

		Settings settings = new Settings();
		Server server = new Server();
		settings.addServer(server);

		mojo.setSettings(settings);

		mojo.setUsername(null);
		mojo.setPassword(null);
		mojo.setUsernameProperty(USERNAME_PROPERTY);
		mojo.setPasswordProperty(PASSWORD_PROPERTY);
		mojo.setUseSystemProperties(false);

		mojo.execute();

		assertEquals("", mojo.getUsername());
		assertEquals("", mojo.getPassword());
	}

	@Test
	public void testUsernamePasswordLookup() throws MojoExecutionException {

		Settings settings = new Settings();
		Server server = new Server();
		server.setId(KEY);
		server.setUsername(USERNAME);
		server.setPassword(PASSWORD);
		settings.addServer(server);

		mojo.setSettings(settings);

		// force a lookup of username
		mojo.setSettingsKey(KEY);
		mojo.setUsernameProperty(USERNAME_PROPERTY);
		mojo.setPasswordProperty(PASSWORD_PROPERTY);
		mojo.setUseSystemProperties(true);

		mojo.execute();

		assertEquals(USERNAME, mojo.getUsername());
		assertEquals(PASSWORD, mojo.getPassword());
		assertEquals(USERNAME, System.getProperty(USERNAME_PROPERTY));
		assertEquals(PASSWORD, System.getProperty(PASSWORD_PROPERTY));
	}

	@Test
	public void testLookupSettingsKeyOnlySystemProperties()
			throws MojoExecutionException {

		Settings settings = new Settings();
		Server server = new Server();
		server.setId(KEY);
		server.setUsername(USERNAME);
		server.setPassword(PASSWORD);
		settings.addServer(server);

		mojo.setSettings(settings);

		// force a lookup of username
		mojo.setSettingsKey(KEY);
		mojo.setUseSystemProperties(true);

		mojo.execute();

		// assertEquals(USERNAME, mojo.getUsername());
		// assertEquals(PASSWORD, mojo.getPassword());
		assertEquals(USERNAME, System.getProperty(KEY + "." + "username"));
		assertEquals(PASSWORD, System.getProperty(KEY + "." + "password"));
	}
}
