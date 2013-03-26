package org.codehaus.mojo.credentials;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

/**
 * Executes Credentials against a database.
 * 
 * @goal execute
 * @threadSafe
 */
public class CredentialsExecMojo extends AbstractMojo {
	/**
	 * Username. If not given, it will be looked up through
	 * <code>settings.xml</code>'s server with <code>${settingsKey}</code> as
	 * key.
	 * 
	 * @since 1.0
	 * @parameter expression="${username}"
	 */
	private String username;

	/**
	 * Property to which the username will be set. If not given, it will be set
	 * to <code>${settingsKey}.username</code> as key.
	 * 
	 * @since 1.0
	 * @parameter expression="${usernameProperty}"
	 */
	private String usernameProperty;

	/**
	 * (Decrypted) password. If not given, it will be looked up through
	 * <code>settings.xml</code>'s server with <code>${settingsKey}</code> as
	 * key.
	 * 
	 * @since 1.0
	 * @parameter expression="${password}"
	 */
	private String password;

	/**
	 * (Decrypted) password. If not given, it will be looked up through
	 * <code>settings.xml</code>'s server with <code>${settingsKey}</code> as
	 * key.
	 * 
	 * @since 1.0
	 * @parameter expression="${password}"
	 */
	private String passwordProperty;

	/**
	 * Whether to set system properties (instead of the default Maven project
	 * properties)
	 * 
	 * @since 1.0
	 * @parameter expression="${useSystemProperties}"
	 */
	private boolean useSystemProperties;

	/**
	 * @parameter expression="${settings}"
	 * @required
	 * @since 1.0
	 * @readonly
	 */
	private Settings settings;

	/**
	 * Server's <code>id</code> in <code>settings.xml</code> to look up username
	 * and password. Defaults to <code>${url}</code> if not given.
	 * 
	 * @since 1.0
	 * @parameter expression="${settingsKey}"
	 */
	private String settingsKey;

	/**
	 * MNG-4384
	 * 
	 * @since 1.5
	 * @component role=
	 *            "hidden.org.sonatype.plexus.components.sec.dispatcher.SecDispatcher"
	 * @required
	 */
	private SecDispatcher securityDispatcher;

	/**
	 * The Maven Project Object
	 * 
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * @parameter default-value="${session}"
	 * @required
	 * @readonly
	 */
	private MavenSession mavenSession;

	// ////////////////////////////// Source info /////////////////////////////

	/**
	 * Load username and password from settings if user has not set them in JVM
	 * properties
	 * 
	 * @throws MojoExecutionException
	 */
	public void execute() throws MojoExecutionException {
		if ((getUsername() == null || getPassword() == null)
				&& (settings != null)) {
			Server server = this.settings.getServer(this.settingsKey);

			if (server != null) {
				if (getUsername() == null) {
					setUsername(server.getUsername());
				}

				if (getPassword() == null && server.getPassword() != null) {
					try {
						setPassword(securityDispatcher.decrypt(server
								.getPassword()));
					} catch (SecDispatcherException e) {
						throw new MojoExecutionException(e.getMessage());
					}
				}
			}
		}

		if (getUsername() == null) {
			setUsername("");
		}

		if (getPassword() == null) {
			setPassword("");
		}

		if (useSystemProperties) {
			System.setProperty(usernameProperty, username);
			System.setProperty(passwordProperty, password);
		} else {
			project.getProperties().setProperty(usernameProperty, username);
			project.getProperties().setProperty(passwordProperty, password);
		}
	}

	String getUsername() {
		return this.username;
	}

	void setUsername(String username) {
		this.username = username;
	}

	final String getUsernameProperty() {
		return usernameProperty;
	}

	final void setUsernameProperty(String usernameProperty) {
		this.usernameProperty = usernameProperty;
	}

	String getPassword() {
		return this.password;
	}

	void setPassword(String password) {
		this.password = password;
	}

	final String getPasswordProperty() {
		return passwordProperty;
	}

	final void setPasswordProperty(String passwordProperty) {
		this.passwordProperty = passwordProperty;
	}

	final boolean isUseSystemProperties() {
		return useSystemProperties;
	}

	final void setUseSystemProperties(boolean useSystemProperties) {
		this.useSystemProperties = useSystemProperties;
	}

	void setSettings(Settings settings) {
		this.settings = settings;
	}

	void setSettingsKey(String key) {
		this.settingsKey = key;
	}

	public void setSecurityDispatcher(SecDispatcher securityDispatcher) {
		this.securityDispatcher = securityDispatcher;
	}
}
